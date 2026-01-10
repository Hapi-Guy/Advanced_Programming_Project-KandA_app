# KnA Android - Quick Start README

## 📱 Q&A Platform for KUET Students

A native Android application migrated from JavaFX desktop app. Features gamified coin-based rewards for knowledge sharing.

## 🚀 Quick Start

### 1. Open in Android Studio
```
File → Open → C:\Users\Shomik\Desktop\okAnd
```

### 2. Wait for Gradle Sync
(Automatic - takes 2-5 min on first run)

### 3. Run App
- Click green Run button (▶) or press `Shift+F10`
- Choose device/emulator
- Recommended: Pixel 5 with API 34

### 4. Test Login
Register a new account or use:
- **Email**: `test@kuet.ac.bd`  
- **Password**: `password123`

## ✨ Features

### ✅ Implemented
- **Authentication**: Login/Register with BCrypt
- **Quora-style Home Feed**: Card-based question display
- **Ask Question**: Title, description, category, urgent flag
- **Filters**: For You, My Dept, Urgent
- **Coin System**: 20 coins/question, 30 for urgent
- **Bottom Navigation**: Home, Notifications, Ask, Leaderboard, Profile
- **Pull-to-Refresh**: Update feed

### 🚧 Partial/Placeholder
- Question detail with answers
- Voting & answer submission
- Notifications list
- Leaderboard
- Profile stats
- Admin panel

## 🏗️ Architecture

**Pattern**: MVVM (Model-View-ViewModel)

```
View (Activity/Fragment)
    ↓
ViewModel (business logic)
    ↓
Repository (data abstraction)
    ↓
Room DAO (database)
    ↓
SQLite Database
```

## 📦 Tech Stack

- **Language**: Java
- **Database**: Room (SQLite)
- **UI**: XML layouts (Material Design 3)
- **Architecture**: MVVM
- **Min SDK**: 21 (Lollipop)
- **Target SDK**: 34 (Android 14)

## 📂 Project Structure

```
app/src/main/java/com/kna/android/
├── data/
│   ├── dao/           # Database queries
│   ├── model/         # Entities (User, Question, Answer)
│   └── repository/    # Business logic
├── ui/
│   ├── adapter/       # RecyclerView adapters
│   ├── auth/          # Login/Register
│   ├── fragment/      # Home, Profile, etc.
│   └── viewmodel/     # MVVM ViewModels
└── util/              # SessionManager, Constants, Validation
```

## 📋 Business Rules (from Desktop App)

| Rule | Value |
|------|-------|
| Base question cost | 20 coins |
| Urgent question cost | 30 coins |
| Reputation per accepted answer | 50 points |
| Urgent bonus (< 30 min) | 2x coins & reputation |
| Rating penalty (< 2 stars) | Coins halved, no reputation |
| Max unevaluated questions | 5 per user |
| Initial coins (new user) | 100 |

## 🔧 Troubleshooting

**Build fails?**
- `File → Invalidate Caches → Restart`
- Delete `.gradle` folder

**App crashes?**
- Check Logcat
- Verify database initialization

**No questions in feed?**
- Register and post a question
- Pull to refresh

## 📄 Documentation

- **Setup Guide**: `SETUP_GUIDE.md` (detailed instructions)
- **Migration Mapping**: `MIGRATION_MAPPING.md` (Desktop → Android)

## 🔮 Future Work

### Priority 1
- [ ] Complete Question Detail screen
- [ ] Answer submission & voting
- [ ] Notifications with RecyclerView
- [ ] Leaderboard display

### Priority 2
- [ ] Image upload
- [ ] Search functionality
- [ ] Admin panel
- [ ] Profile stats

### Priority 3
- [ ] Firebase integration
- [ ] Push notifications
- [ ] Real-time updates
- [ ] Offline mode

## 📊 Migration Status

- **Models**: 95% complete
- **Business Logic**: 90% complete
- **Database**: 100% complete
- **UI**: 60% complete
- **Overall**: ~65% complete

## 🎨 UI Highlights

### Home Screen (Quora-inspired)
- Top bar: Search, KnA logo, Add Question
- Ask bar: "What do you want to ask?"
- Filter chips: For You, Following, My Dept, Urgent
- Question cards: User info, title, description, badges, coins
- Bottom nav: 5 tabs

### Authentication
- Material Design 3 text inputs
- User/Admin radio buttons
- Form validation
- Password hashing

## 🔒 Security

- BCrypt password hashing (cost factor: 12)
- Email validation (regex)
- SQL injection prevention (Room parameterized queries)
- Session persistence (SharedPreferences)

## 📞 Support

**Issues?**
- Check Android Logcat
- Review database schema in `app/schemas/`
- Refer to `SETUP_GUIDE.md`

---

**Version**: 1.0.0  
**Last Updated**: January 9, 2026  
**Author**: Migrated from JavaFX KnA Desktop App
