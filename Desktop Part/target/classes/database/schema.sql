-- KnA Application Database Schema
-- SQLite Database with all required tables

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    phone TEXT,
    password_hash TEXT NOT NULL,
    name TEXT NOT NULL,
    department TEXT NOT NULL,
    academic_year INTEGER NOT NULL,
    coins INTEGER DEFAULT 100,
    reputation INTEGER DEFAULT 0,
    total_questions INTEGER DEFAULT 0,
    total_answers INTEGER DEFAULT 0,
    accepted_answers INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT 1,
    is_admin BOOLEAN DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sessions table for login persistence
CREATE TABLE IF NOT EXISTS sessions (
    session_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    session_token TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Questions table
CREATE TABLE IF NOT EXISTS questions (
    question_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    category TEXT NOT NULL,
    is_urgent BOOLEAN DEFAULT 0,
    coin_reward INTEGER NOT NULL,
    is_answered BOOLEAN DEFAULT 0,
    is_evaluated BOOLEAN DEFAULT 0,
    accepted_answer_id INTEGER,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Question images table
CREATE TABLE IF NOT EXISTS question_images (
    image_id INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id INTEGER NOT NULL,
    image_path TEXT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- Answers table
CREATE TABLE IF NOT EXISTS answers (
    answer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    is_accepted BOOLEAN DEFAULT 0,
    rating INTEGER DEFAULT 0,
    upvotes INTEGER DEFAULT 0,
    downvotes INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Answer votes table
CREATE TABLE IF NOT EXISTS answer_votes (
    vote_id INTEGER PRIMARY KEY AUTOINCREMENT,
    answer_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    vote_type TEXT CHECK(vote_type IN ('upvote', 'downvote')) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (answer_id) REFERENCES answers(answer_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE(answer_id, user_id)
);

-- Answer rewards table
CREATE TABLE IF NOT EXISTS answer_rewards (
    reward_id INTEGER PRIMARY KEY AUTOINCREMENT,
    answer_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    coins_earned INTEGER NOT NULL,
    reputation_earned INTEGER NOT NULL,
    is_bonus BOOLEAN DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (answer_id) REFERENCES answers(answer_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Coin transactions table
CREATE TABLE IF NOT EXISTS coin_transactions (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    amount INTEGER NOT NULL,
    transaction_type TEXT CHECK(transaction_type IN ('earned', 'spent', 'purchased', 'refund')) NOT NULL,
    description TEXT,
    reference_id INTEGER,
    reference_type TEXT,
    balance_after INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Coin purchases table
CREATE TABLE IF NOT EXISTS coin_purchases (
    purchase_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    coins_purchased INTEGER NOT NULL,
    amount_paid DECIMAL(10, 2) NOT NULL,
    payment_method TEXT DEFAULT 'simulated',
    payment_status TEXT DEFAULT 'completed',
    transaction_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_id) REFERENCES coin_transactions(transaction_id)
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    notification_type TEXT CHECK(notification_type IN ('answer', 'accepted', 'earned', 'low_balance', 'announcement', 'warning')) NOT NULL,
    is_read BOOLEAN DEFAULT 0,
    reference_id INTEGER,
    reference_type TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Admins table
CREATE TABLE IF NOT EXISTS admins (
    admin_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER UNIQUE NOT NULL,
    admin_level INTEGER DEFAULT 1,
    permissions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Reports table
CREATE TABLE IF NOT EXISTS reports (
    report_id INTEGER PRIMARY KEY AUTOINCREMENT,
    reporter_user_id INTEGER NOT NULL,
    reported_type TEXT CHECK(reported_type IN ('question', 'answer', 'user')) NOT NULL,
    reported_id INTEGER NOT NULL,
    reason TEXT NOT NULL,
    description TEXT,
    status TEXT DEFAULT 'pending' CHECK(status IN ('pending', 'reviewed', 'resolved', 'dismissed')),
    admin_notes TEXT,
    resolved_by INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    FOREIGN KEY (reporter_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (resolved_by) REFERENCES admins(admin_id)
);

-- Leaderboard table (materialized view for performance)
CREATE TABLE IF NOT EXISTS leaderboard (
    leaderboard_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    period TEXT CHECK(period IN ('weekly', 'monthly', 'all_time')) NOT NULL,
    reputation INTEGER NOT NULL,
    rank INTEGER NOT NULL,
    period_start DATE,
    period_end DATE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_sessions_token ON sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_sessions_user ON sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_questions_user ON questions(user_id);
CREATE INDEX IF NOT EXISTS idx_questions_category ON questions(category);
CREATE INDEX IF NOT EXISTS idx_questions_urgent ON questions(is_urgent);
CREATE INDEX IF NOT EXISTS idx_questions_answered ON questions(is_answered);
CREATE INDEX IF NOT EXISTS idx_questions_evaluated ON questions(is_evaluated);
CREATE INDEX IF NOT EXISTS idx_questions_created ON questions(created_at);
CREATE INDEX IF NOT EXISTS idx_answers_question ON answers(question_id);
CREATE INDEX IF NOT EXISTS idx_answers_user ON answers(user_id);
CREATE INDEX IF NOT EXISTS idx_answer_votes_answer ON answer_votes(answer_id);
CREATE INDEX IF NOT EXISTS idx_coin_transactions_user ON coin_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status);
CREATE INDEX IF NOT EXISTS idx_leaderboard_period ON leaderboard(period, rank);

-- Insert default admin user (password: admin123)
INSERT OR IGNORE INTO users (user_id, email, password_hash, name, department, academic_year, coins, reputation, is_admin)
VALUES (1, 'admin@kna.com', 'admin123', 'Admin User', 'Administration', 0, 10000, 10000, 1);

INSERT OR IGNORE INTO admins (user_id, admin_level, permissions)
VALUES (1, 3, 'all');

-- Insert sample users for testing (password: test123)
INSERT OR IGNORE INTO users (email, password_hash, name, department, academic_year, coins, reputation)
VALUES 
('john.doe@kna.com', 'test123', 'John Doe', 'CSE', 3, 150, 250),
('jane.smith@kna.com', 'test123', 'Jane Smith', 'EEE', 2, 200, 400),
('bob.wilson@kna.com', 'test123', 'Bob Wilson', 'CE', 4, 100, 150);

-- Insert sample questions
INSERT OR IGNORE INTO questions (user_id, title, description, category, is_urgent, coin_reward, is_answered, is_evaluated)
VALUES 
(2, 'How to implement Dijkstra algorithm?', 'I need help understanding the Dijkstra shortest path algorithm implementation in Java.', 'CSE', 0, 20, 0, 0),
(3, 'Explain Kirchhoff voltage law', 'Can someone explain KVL with a practical circuit example?', 'EEE', 1, 30, 0, 0),
(4, 'Best practices for concrete mixing', 'What are the standard ratios for concrete mixing in construction?', 'CE', 0, 15, 0, 0);
