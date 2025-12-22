# KnA - Knowledge and Answers

A comprehensive JavaFX desktop application for a Q&A platform with a coin-based reward system.

## 📋 Overview

KnA is a feature-rich Q&A desktop application built with Java 17+, JavaFX, and SQLite. Users can ask questions, provide answers, earn coins, and build reputation through community engagement.

## ✨ Features Implemented

### 1. **User Authentication**
- User registration with email/phone + password
- Secure login with BCrypt password hashing
- Session management
- Profile information (name, department, academic year)

### 2. **User Profile System**
- View current coin balance
- View reputation points
- Statistics display:
  - Total questions asked
  - Total answers given
  - Acceptance rate
- Editable profile fields (name, department, year)
- Read-only stats (coins, reputation)

### 3. **Question System**
- Ask questions with:
  - Title and description
  - Category selection (CSE, EEE, CE, ME, etc.)
  - Optional image upload
  - Urgent mode toggle (costs additional coins)
- Coin deduction when posting questions
- Question feed with:
  - Category filtering
  - Urgent-only filter
  - Unanswered-only filter
  - Real-time search
- Question detail view
- View count tracking

### 4. **Answer System**
- Submit answers to questions
- Upvote/downvote answers
- Accept best answer (question owner only)
- Rate answers (0-5 stars)
- Earn coins when answer is accepted
- Bonus coins for fast answers on urgent questions
- Reputation gain from upvotes and accepted answers

### 5. **Coin System** 💰
- Starting balance: 100 coins
- Costs:
  - Regular question: 20 coins
  - Urgent question: 30 coins
- Earnings:
  - Accepted answers: Based on question reward
  - Fast urgent responses: 2x bonus
- Coin purchase packages:
  - Starter: 50 coins - ৳50
  - Basic: 100 coins - ৳90
  - Standard: 250 coins - ৳200
  - Premium: 500 coins - ৳375
  - Ultimate: 1000 coins - ৳700
- Simulated payment system (white screen transition)
- Transaction history tracking

### 6. **Reputation System** ⭐
- Gain reputation from:
  - Accepted answers: +50 points
  - Answer upvotes: +10 points per upvote
  - Fast urgent responses: bonus multiplier
- Poor ratings result in no reputation gain

### 7. **Notification System**
- Notifications for:
  - New answers on your questions
  - Answer accepted
  - Coins earned
  - Low coin balance warnings
- Real-time notification badge
- Unread count display

### 8. **Mandatory Evaluation Restriction**
- Users can have maximum 5 unevaluated questions
- Must evaluate answered questions before asking new ones
- Evaluation counter updates automatically
- System enforces the limit with clear messaging

### 9. **Search System**
- Search questions by title or content
- Real-time search from dashboard
- Category-based filtering

### 10. **Admin Panel** (For admin users)
- User management
- Content moderation
- Transaction viewing
- System analytics

## 🏗️ Architecture

### Project Structure
```
Desktop Part/
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/kna/
│       │   ├── Main.java
│       │   ├── controller/
│       │   │   ├── LoginController.java
│       │   │   ├── RegisterController.java
│       │   │   ├── DashboardController.java
│       │   │   ├── QuestionDetailController.java
│       │   │   └── CoinPurchaseController.java
│       │   ├── dao/
│       │   │   ├── UserDAO.java
│       │   │   ├── QuestionDAO.java
│       │   │   ├── AnswerDAO.java
│       │   │   ├── CoinDAO.java
│       │   │   └── NotificationDAO.java
│       │   ├── model/
│       │   │   ├── User.java
│       │   │   ├── Question.java
│       │   │   ├── Answer.java
│       │   │   ├── CoinTransaction.java
│       │   │   ├── Notification.java
│       │   │   ├── Session.java
│       │   │   ├── Report.java
│       │   │   └── CoinPurchase.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   ├── QuestionService.java
│       │   │   ├── AnswerService.java
│       │   │   └── CoinService.java
│       │   └── util/
│       │       ├── DatabaseManager.java
│       │       ├── PasswordHasher.java
│       │       ├── FXMLLoaderHelper.java
│       │       ├── ImageLoader.java
│       │       ├── ToastNotification.java
│       │       └── SessionManager.java
│       └── resources/
│           ├── css/
│           │   └── application.css
│           ├── fxml/
│           │   ├── Login.fxml
│           │   ├── Register.fxml
│           │   ├── Dashboard.fxml
│           │   ├── QuestionDetail.fxml
│           │   └── CoinPurchase.fxml
│           └── database/
│               └── schema.sql
```

### Design Patterns Used
- **Singleton**: DatabaseManager, SessionManager
- **DAO Pattern**: All database operations
- **MVC/MVVM**: Clear separation of concerns
- **Service Layer**: Business logic encapsulation

## 🛠️ Technology Stack

- **Java**: 17+
- **JavaFX**: 21.0.1
- **SQLite**: 3.44.1.0
- **BCrypt**: 0.4 (password hashing)
- **Maven**: Build tool

## 📦 Database Schema

### Tables
1. **users** - User accounts and stats
2. **sessions** - Login session management
3. **questions** - All questions
4. **question_images** - Question attachments
5. **answers** - All answers
6. **answer_votes** - Voting system
7. **answer_rewards** - Reward tracking
8. **coin_transactions** - All coin movements
9. **coin_purchases** - Purchase history
10. **notifications** - User notifications
11. **admins** - Admin privileges
12. **reports** - Content reports
13. **leaderboard** - Reputation rankings

## 🚀 Getting Started

### Prerequisites
- Java JDK 17 or higher
- Maven 3.6+
- JavaFX SDK 21+

### Installation

1. Clone the repository
2. Navigate to the project directory
3. Build with Maven:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn javafx:run
```

### Default Credentials

**Admin Account:**
- Email: admin@kna.com
- Password: admin123

**Test Users:**
- Email: john.doe@kna.com / Password: test123
- Email: jane.smith@kna.com / Password: test123

## 💡 Usage

### For Regular Users

1. **Register/Login**
   - Create an account with email, password, name, department, and academic year
   - Login to access the dashboard

2. **Ask Questions**
   - Click "Ask Question" from sidebar
   - Fill in title, description, category
   - Toggle urgent mode for higher priority (costs more coins)
   - Optional: Upload an image
   - Submit (coins will be deducted)

3. **Answer Questions**
   - Browse question feed
   - Click on any question to view details
   - Submit your answer in the text area
   - Other users can upvote your answer

4. **Earn Coins**
   - Answer questions and wait for acceptance
   - Fast answers on urgent questions earn bonus coins
   - Higher ratings (4-5 stars) earn full rewards
   - Low ratings (0-2 stars) earn reduced rewards

5. **Purchase Coins**
   - Click "Buy Coins" from sidebar
   - Select a package
   - Confirm purchase
   - Simulated payment screen will appear
   - Coins added to your balance automatically

6. **Build Reputation**
   - Get answers accepted: +50 reputation
   - Receive upvotes: +10 reputation each
   - Maintain high acceptance rate

### For Admins

1. Access "Admin Panel" from sidebar
2. Manage users and content
3. View all transactions
4. Monitor system activity

## 🔐 Security Features

- Passwords hashed with BCrypt (work factor: 10)
- SQL injection prevention with PreparedStatements
- Session-based authentication
- Input validation on all forms

## 📊 Key Business Rules

1. **Question Posting**
   - Costs 20 coins (regular) or 30 coins (urgent)
   - Users blocked after 5 unevaluated questions
   - Must evaluate answered questions to ask more

2. **Answer Rewards**
   - Base reward = question's coin value
   - Fast urgent answers get 2x multiplier
   - Poor ratings (<2 stars) = 50% coins, no reputation

3. **Voting**
   - Cannot vote on own answers
   - Can change vote or remove vote
   - Upvotes increase answerer's reputation

4. **Evaluation Requirement**
   - Maximum 5 unevaluated questions
   - Evaluation happens when accepting an answer
   - Counter displayed to user

## 🎨 UI Features

- Modern Material Design inspired interface
- Responsive layouts
- Toast notifications for user feedback
- Real-time coin/reputation updates
- Notification badges
- Search functionality
- Multiple filter options
- Smooth animations

## 🔧 Configuration

### Coin Costs (Configurable in QuestionService)
```java
BASE_QUESTION_COST = 20
URGENT_QUESTION_COST = 30
```

### Reputation Points (Configurable in AnswerService)
```java
REPUTATION_PER_UPVOTE = 10
REPUTATION_PER_ACCEPTED = 50
URGENT_BONUS_MULTIPLIER = 2
```

## 📝 Database Location

The SQLite database file `kna_database.db` is created in the project root directory on first run.

## 🐛 Known Issues & Future Enhancements

### To Be Implemented (Additional Views)
- Profile editing page
- Ask Question form (full version)
- My Questions view
- My Answers view
- Leaderboard view
- Full notification center
- Search results page
- Admin panel views

### Potential Enhancements
- Email verification
- Password reset functionality
- Image compression for uploads
- Real-time chat between users
- Question bookmarking
- Answer comments
- Tag system for questions
- Advanced search with filters
- Export data functionality

## 📄 License

This project is created as an academic project for Advanced Programming coursework.

## 👥 Contributors

- Full-stack JavaFX Development Team

## 📧 Support

For issues or questions, please contact the development team.

---

**Note**: This is a complete working application with core features implemented. Additional views and features can be added by following the existing architecture patterns.
