package com.kna.android.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.kna.android.data.dao.AnswerDao;
import com.kna.android.data.dao.AnswerVoteDao;
import com.kna.android.data.dao.CoinTransactionDao;
import com.kna.android.data.dao.NotificationDao;
import com.kna.android.data.dao.QuestionDao;
import com.kna.android.data.dao.UserDao;
import com.kna.android.data.database.KnADatabase;
import com.kna.android.data.model.Answer;
import com.kna.android.data.model.AnswerVote;
import com.kna.android.data.model.AnswerWithUser;
import com.kna.android.data.model.CoinTransaction;
import com.kna.android.data.model.Notification;
import com.kna.android.data.model.Question;
import com.kna.android.data.model.User;
import com.kna.android.util.Constants;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Answer Repository - migrated from desktop AnswerService.java
 * Handles answer submission, acceptance, voting, and reward calculation
 */
public class AnswerRepository {
    
    private final AnswerDao answerDao;
    private final QuestionDao questionDao;
    private final UserDao userDao;
    private final AnswerVoteDao voteDao;
    private final CoinTransactionDao coinTransactionDao;
    private final NotificationDao notificationDao;
    private final KnADatabase database;
    
    public AnswerRepository(Application application) {
        database = KnADatabase.getDatabase(application);
        answerDao = database.answerDao();
        questionDao = database.questionDao();
        userDao = database.userDao();
        voteDao = database.answerVoteDao();
        coinTransactionDao = database.coinTransactionDao();
        notificationDao = database.notificationDao();
    }
    
    /**
     * Submit answer
     */
    public Future<SubmitAnswerResult> submitAnswer(long userId, long questionId, String content) {
        return KnADatabase.databaseWriteExecutor.submit(() -> {
            // Validate content
            if (content == null || content.trim().isEmpty()) {
                return new SubmitAnswerResult(false, "Answer content is required");
            }
            
            // Create answer
            Answer answer = new Answer();
            answer.setQuestionId(questionId);
            answer.setUserId(userId);
            answer.setContent(content);
            answer.setCreatedAt(System.currentTimeMillis());
            
            long answerId = answerDao.insert(answer);
            answer.setAnswerId(answerId);
            
            // Increment answer count on question
            questionDao.incrementAnswerCount(questionId, System.currentTimeMillis());
            
            // Increment total answers for user
            userDao.incrementTotalAnswers(userId);
            
            // Notify question owner
            Question question = questionDao.getQuestionByIdSync(questionId);
            if (question != null && question.getUserId() != userId) {
                User answerer = userDao.getUserByIdSync(userId);
                Notification notification = new Notification();
                notification.setUserId(question.getUserId());
                notification.setTitle("New Answer");
                notification.setMessage(answerer.getName() + " answered your question: " + question.getTitle());
                notification.setType("ANSWER");
                notification.setReferenceId(questionId);
                notificationDao.insert(notification);
            }
            
            return new SubmitAnswerResult(true, "Answer submitted successfully", answer);
        });
    }
    
    /**
     * Accept answer - from desktop AnswerService with reward calculation
     * Business rules:
     * - Base reward: question's coin_reward
     * - Base reputation: 50 points (REPUTATION_PER_ACCEPTED)
     * - Urgent bonus (answered within 30 min): 2x coins and reputation
     * - Rating penalty (rating < 2): coins halved, reputation = 0
     */
    public Future<AcceptAnswerResult> acceptAnswer(long questionId, long answerId, int rating) {
        return KnADatabase.databaseWriteExecutor.submit(() -> {
            Question question = questionDao.getQuestionByIdSync(questionId);
            Answer answer = answerDao.getAnswerByIdSync(answerId);
            
            if (question == null || answer == null) {
                return new AcceptAnswerResult(false, "Question or answer not found");
            }
            
            // Check if already answered
            if (question.isAnswered()) {
                return new AcceptAnswerResult(false, "Question already has an accepted answer");
            }
            
            // Update answer rating
            answerDao.updateRating(answerId, rating);
            
            // Mark answer as accepted
            answerDao.markAsAccepted(answerId);
            answerDao.unmarkOthersAsAccepted(questionId, answerId);
            
            // Mark question as answered
            questionDao.markAsAnswered(questionId, answerId, System.currentTimeMillis());
            
            // Calculate rewards
            int coins = question.getCoinReward();
            int reputation = Constants.REPUTATION_PER_ACCEPTED;
            
            // Urgent bonus (within 30 minutes)
            long timeDiff = (answer.getCreatedAt() - question.getCreatedAt()) / 1000 / 60; // minutes
            if (question.isUrgent() && timeDiff <= 30) {
                coins *= 2;
                reputation *= 2;
            }
            
            // Rating penalty (rating < 2)
            if (rating < 2) {
                coins /= 2;
                reputation = 0;
            }
            
            // Award coins and reputation to answerer
            userDao.addCoins(answer.getUserId(), coins);
            userDao.addReputation(answer.getUserId(), reputation);
            
            // Increment accepted answers count
            userDao.incrementAcceptedAnswers(answer.getUserId());
            
            // Record coin transaction
            User answerer = userDao.getUserByIdSync(answer.getUserId());
            CoinTransaction transaction = new CoinTransaction();
            transaction.setUserId(answer.getUserId());
            transaction.setAmount(coins);
            transaction.setTransactionType("EARN");
            transaction.setDescription("Answer accepted for question: " + question.getTitle());
            transaction.setBalanceAfter(answerer.getCoins());
            coinTransactionDao.insert(transaction);
            
            // Notify answerer
            Notification notification = new Notification();
            notification.setUserId(answer.getUserId());
            notification.setTitle("Answer Accepted!");
            notification.setMessage("Your answer was accepted! You earned " + coins + " coins and " + reputation + " reputation.");
            notification.setType("ACCEPTED");
            notification.setReferenceId(answerId);
            notificationDao.insert(notification);
            
            return new AcceptAnswerResult(true, "Answer accepted successfully", coins, reputation);
        });
    }
    
    /**
     * Vote on answer (upvote/downvote)
     * Business rules:
     * - Cannot vote on own answer
     * - One vote per user per answer
     */
    public Future<VoteResult> voteAnswer(long userId, long answerId, boolean isUpvote) {
        return KnADatabase.databaseWriteExecutor.submit(() -> {
            Answer answer = answerDao.getAnswerByIdSync(answerId);
            
            if (answer == null) {
                return new VoteResult(false, "Answer not found");
            }
            
            // Cannot vote on own answer
            if (answer.getUserId() == userId) {
                return new VoteResult(false, "You cannot vote on your own answer");
            }
            
            // Check if already voted
            AnswerVote existingVote = voteDao.getUserVote(answerId, userId);
            
            if (existingVote != null) {
                // Already voted - toggle or remove
                int existingVoteType = existingVote.getVoteType();
                int newVoteType = isUpvote ? 1 : -1;
                
                if (existingVoteType == newVoteType) {
                    // Same vote - remove it
                    voteDao.delete(existingVote);
                    if (isUpvote) {
                        answerDao.decrementUpvotes(answerId);
                    } else {
                        answerDao.decrementDownvotes(answerId);
                    }
                    return new VoteResult(true, "Vote removed");
                } else {
                    // Different vote - change it
                    existingVote.setVoteType(newVoteType);
                    voteDao.insert(existingVote);
                    
                    if (isUpvote) {
                        answerDao.incrementUpvotes(answerId);
                        answerDao.decrementDownvotes(answerId);
                    } else {
                        answerDao.incrementDownvotes(answerId);
                        answerDao.decrementUpvotes(answerId);
                    }
                    return new VoteResult(true, "Vote changed");
                }
            } else {
                // New vote
                AnswerVote vote = new AnswerVote();
                vote.setAnswerId(answerId);
                vote.setUserId(userId);
                vote.setVoteType(isUpvote ? 1 : -1);
                voteDao.insert(vote);
                
                if (isUpvote) {
                    answerDao.incrementUpvotes(answerId);
                } else {
                    answerDao.incrementDownvotes(answerId);
                }
                
                // Notify answer owner
                User voter = userDao.getUserByIdSync(userId);
                Notification notification = new Notification();
                notification.setUserId(answer.getUserId());
                notification.setTitle(isUpvote ? "Upvote" : "Downvote");
                notification.setMessage(voter.getName() + (isUpvote ? " upvoted" : " downvoted") + " your answer");
                notification.setType("VOTE");
                notification.setReferenceId(answerId);
                notificationDao.insert(notification);
                
                return new VoteResult(true, isUpvote ? "Upvoted" : "Downvoted");
            }
        });
    }
    
    /**
     * Get answers for question
     */
    public LiveData<List<AnswerWithUser>> getAnswersForQuestion(long questionId) {
        return answerDao.getAnswersForQuestion(questionId);
    }
    
    /**
     * Get answers by user
     */
    public LiveData<List<Answer>> getAnswersByUser(long userId) {
        return answerDao.getAnswersByUser(userId);
    }
    
    /**
     * Get user's vote for answer
     */
    public Future<Integer> getUserVoteType(long userId, long answerId) {
        return KnADatabase.databaseWriteExecutor.submit(() -> {
            AnswerVote vote = voteDao.getUserVote(answerId, userId);
            return vote != null ? vote.getVoteType() : 0;
        });
    }
    
    // Result classes
    public static class SubmitAnswerResult {
        public final boolean success;
        public final String message;
        public final Answer answer;
        
        public SubmitAnswerResult(boolean success, String message) {
            this(success, message, null);
        }
        
        public SubmitAnswerResult(boolean success, String message, Answer answer) {
            this.success = success;
            this.message = message;
            this.answer = answer;
        }
    }
    
    public static class AcceptAnswerResult {
        public final boolean success;
        public final String message;
        public final int coinsAwarded;
        public final int reputationAwarded;
        
        public AcceptAnswerResult(boolean success, String message) {
            this(success, message, 0, 0);
        }
        
        public AcceptAnswerResult(boolean success, String message, int coinsAwarded, int reputationAwarded) {
            this.success = success;
            this.message = message;
            this.coinsAwarded = coinsAwarded;
            this.reputationAwarded = reputationAwarded;
        }
    }
    
    public static class VoteResult {
        public final boolean success;
        public final String message;
        
        public VoteResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
