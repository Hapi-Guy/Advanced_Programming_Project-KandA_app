# KnA Android App - Setup Guide

## Project Overview
This is a native Android application migrated from the JavaFX desktop KnA app. It's a Q&A platform for KUET students with a gamified coin-based reward system.

## Prerequisites
- **Android Studio**: Flamingo (2022.2.1) or later
- **JDK**: Version 17
- **Minimum Android SDK**: 21 (Android 5.0 Lollipop)
- **Target Android SDK**: 34 (Android 14)

## How to Run in Android Studio

### Step 1: Open Project
1. Launch Android Studio
2. Click **File → Open**
3. Navigate to: `C:\Users\Shomik\Desktop\okAnd`
4. Click **OK**

### Step 2: Gradle Sync
1. Android Studio will automatically start Gradle sync
2. Wait for the process to complete (may take 2-5 minutes on first run)
3. If sync fails, click **File → Sync Project with Gradle Files**

### Step 3: Build Project
1. Click **Build → Make Project** (or press `Ctrl+F9`)
2. Wait for build to complete
3. Check **Build** tab for any errors

### Step 4: Run Application
1. Connect an Android device via USB **OR** create an Android Virtual Device (AVD)
   - For AVD: **Tools → Device Manager → Create Device**
   - Recommended: Pixel 5 with API 34
2. Click the green **Run** button (▶) or press `Shift+F10`
3. Select your device/emulator
4. Wait for app installation and launch

### Step 5: Test Login
**Sample Users** (create via Register screen or use these test accounts):
- **Email**: `test@kuet.ac.bd`
- **Password**: `password123`
- **Type**: User

**Admin Account**:
- **Email**: `admin@kuet.ac.bd`
- **Password**: `admin123`
- **Type**: Admin

## Project Structure

```
app/src/main/
├── java/com/kna/android/
│   ├── data/
│   │   ├── dao/               # Room DAOs (database queries)
│   │   ├── database/          # Room Database singleton
│   │   ├── model/             # Entity classes (User, Question, Answer, etc.)
│   │   └── repository/        # Business logic layer
│   ├── ui/
│   │   ├── adapter/           # RecyclerView adapters
│   │   ├── auth/              # Login & Register activities
│   │   ├── fragment/          # Home, Profile, Leaderboard, Notifications
│   │   ├── main/              # MainActivity with bottom navigation
│   │   ├── question/          # Ask Question & Question Detail
│   │   ├── admin/             # Admin panel
│   │   └── viewmodel/         # MVVM ViewModels
│   ├── util/                  # Utilities (SessionManager, ValidationUtils, etc.)
│   └── KnAApplication.java    # Application class
├── res/
│   ├── layout/                # XML layouts (Quora-style home feed)
│   ├── drawable/              # UI assets (badges, backgrounds)
│   ├── menu/                  # Bottom navigation menu
│   ├── values/                # Strings, colors, themes
│   └── xml/                   # File provider paths, backup rules
└── AndroidManifest.xml
```

## Architecture: MVVM (Model-View-ViewModel)

```
User Interaction (View/Fragment)
    ↓
ViewModel (business logic coordination)
    ↓
Repository (data source abstraction)
    ↓
DAO (Room database queries)
    ↓
SQLite Database (kna_database)
```

## Key Features Implemented

### ✅ Completed
1. **Authentication**
   - Login with User/Admin roles
   - Registration with validation
   - Persistent session (SharedPreferences)
   - BCrypt password hashing

2. **Home Feed (Quora-style)**
   - Card-based question display
   - User avatar, name, department
   - Question title, description preview
   - Category tags, urgent badges
   - Coin reward indicators
   - Answer count
   - Pull-to-refresh
   - Filter tabs (For You, Following, My Dept, Urgent)

3. **Ask Question**
   - Title & description input
   - Category dropdown
   - Urgent checkbox (extra coin cost)
   - Coin cost calculation
   - Validation rules

4. **Database (Room)**
   - User, Question, Answer, Notification, CoinTransaction entities
   - LiveData for reactive updates
   - Foreign key relationships
   - Business logic preserved (coin deduction, reputation, rewards)

5. **Business Rules (from Desktop App)**
   - Base question cost: 20 coins
   - Urgent question cost: 30 coins
   - Reputation per accepted answer: 50
   - Urgent bonus (within 30 min): 2x coins & reputation
   - Rating penalty (< 2 stars): coins halved, reputation = 0
   - Max 5 unevaluated questions per user

### 🚧 Placeholder (TODO)
- Question Detail screen (basic implementation exists)
- Answer submission & voting
- Notifications list
- Leaderboard with RecyclerView
- Profile screen with stats
- Admin panel
- Image upload for questions
- Search functionality

## Database Details

### Tables Created
- `users` - User profiles, coins, reputation
- `questions` - Questions with categories, urgency, coin rewards
- `answers` - Answers with ratings, votes
- `answer_votes` - Upvote/downvote records
- `notifications` - User notifications
- `coin_transactions` - Transaction history

### Database Location
- **Development**: `/data/data/com.kna.android/databases/kna_database`
- Access via **Device File Explorer** in Android Studio

## Troubleshooting

### Build Errors
**Problem**: Gradle sync fails
**Solution**: 
- Check internet connection
- **File → Invalidate Caches → Invalidate and Restart**
- Delete `.gradle` folder and sync again

**Problem**: Room schema errors
**Solution**: 
- **Build → Clean Project**
- **Build → Rebuild Project**

### Runtime Errors
**Problem**: App crashes on login
**Solution**: 
- Check Logcat for errors
- Ensure database is initialized (check `KnAApplication.onCreate()`)

**Problem**: No questions in feed
**Solution**: 
- Register a new user
- Post a question via "Ask" screen
- Refresh home feed

### Emulator Issues
**Problem**: Emulator is slow
**Solution**: 
- Use **Google Play** system image (not Google APIs)
- Enable hardware acceleration: **Tools → AVD Manager → Edit → Show Advanced → Graphics: Hardware**

## Next Steps for Development

### Priority 1: Complete Core Features
1. Finish Question Detail screen with answers RecyclerView
2. Implement answer submission and acceptance
3. Add upvote/downvote functionality
4. Complete notifications with push support

### Priority 2: UI Polish
1. Add Material Design 3 animations
2. Implement search functionality
3. Create custom user avatars
4. Add loading states and error handling

### Priority 3: Advanced Features
1. Image upload for questions
2. Real-time updates (consider Firebase)
3. Offline caching improvements
4. Admin panel with user management

## Firebase Integration (Future)
The architecture supports Firebase integration:
- Replace local SQLite with Firestore
- Add Firebase Authentication
- Implement Cloud Messaging for notifications
- Use Firebase Storage for images

All Repository classes are abstracted - just swap data sources!

## Testing Recommendations
1. Test on multiple screen sizes (phone, tablet)
2. Test offline functionality
3. Verify business rules (coin deduction, reputation calculation)
4. Test edge cases (negative coins, duplicate votes)

## Contact & Support
For issues or questions:
- Check Android Logcat for detailed error messages
- Review Room database schema in `app/schemas/`
- Refer to desktop app analysis document

---
**Last Updated**: January 9, 2026
**Version**: 1.0.0
**Min SDK**: 21 | **Target SDK**: 34
