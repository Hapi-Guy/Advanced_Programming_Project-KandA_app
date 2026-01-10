# Testing with Sample Data

## Quick Test - Use DataSeeder

To populate the app with sample users and questions for immediate testing:

### Option 1: Automatic Seeding (Recommended)

Add this to `MainActivity.java` in `onCreate()`:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // Seed database on first launch (development only)
    if (BuildConfig.DEBUG) {
        DataSeeder.seedDatabase(this);
    }
    
    // ... rest of onCreate
}
```

### Option 2: Manual Trigger

1. Open `MainActivity.java`
2. Add a button or menu item
3. Call: `DataSeeder.seedDatabase(this);`

## Sample Accounts Created

After seeding, you can login with:

### Regular Users

**User 1** - Student with pending questions
- Email: `test@kuet.ac.bd`
- Password: `password123`
- Department: CSE, Year 3
- Coins: 150, Reputation: 100

**User 2** - Active Contributor
- Email: `sakib@kuet.ac.bd`
- Password: `password123`
- Department: EEE, Year 4
- Coins: 250, Reputation: 300

**User 3** - New Student
- Email: `nabila@kuet.ac.bd`
- Password: `password123`
- Department: ME, Year 1
- Coins: 80, Reputation: 20

**User 4** - Expert
- Email: `karim@kuet.ac.bd`
- Password: `password123`
- Department: CSE, Year 5
- Coins: 500, Reputation: 850

### Admin Account

- Email: `admin@kuet.ac.bd`
- Password: `admin123`
- Coins: 1000, Reputation: 1000

## Sample Questions Created

1. **"How to implement binary search tree in Java?"** (Urgent, 30 coins)
   - Posted by Rafiq Ahmed (User 1)
   - Category: Academic Help
   - Has 1 answer from Dr. Karim

2. **"Best resources for learning circuit analysis?"** (20 coins)
   - Posted by Sakib Rahman (User 2)
   - Category: Academic Help
   - Has 1 answer

3. **"Where is the best place to study on campus?"** (20 coins)
   - Posted by Nabila Islam (User 3)
   - Category: Campus Life
   - Has 1 answer

4. **"Android Room vs Realm Database"** (20 coins)
   - Posted by Rafiq Ahmed (User 1)
   - Category: Technical Discussion
   - No answers yet

5. **"Calculus midterm tomorrow!"** (Urgent, 30 coins)
   - Posted by Nabila Islam (User 3)
   - Category: Academic Help
   - No answers yet

6. **"Internship opportunities for CSE students?"** (20 coins)
   - Posted by Sakib Rahman (User 2)
   - Category: Career Advice
   - No answers yet

7. **"How to register for semester courses online?"** (20 coins)
   - Posted by Rafiq Ahmed (User 1)
   - Category: General
   - No answers yet

8. **"Best programming language to learn in 2026?"** (20 coins, ACCEPTED)
   - Posted by Nabila Islam (User 3)
   - Category: Technical Discussion
   - Has 2 answers (one accepted with 5-star rating)

## What Gets Created

- ✅ 5 users (4 regular + 1 admin)
- ✅ 8 questions (2 urgent, 6 regular)
- ✅ 5 answers (including code examples)
- ✅ 1 accepted answer
- ✅ 2 upvotes
- ✅ 2 notifications

## Testing Scenarios

### Scenario 1: View Questions
1. Login as `test@kuet.ac.bd`
2. See 8 questions in Home feed
3. Try filter chips (Urgent shows 2 questions)

### Scenario 2: Ask a Question
1. Login as any user
2. Tap "Ask" button
3. Post a question (deducts 20 or 30 coins)
4. Check feed for new question

### Scenario 3: View Answers
1. Login as `test@kuet.ac.bd`
2. Click on "Best programming language" question
3. See 2 answers (one accepted with green checkmark)

### Scenario 4: Admin Access
1. Login as `admin@kuet.ac.bd`
2. Access admin panel
3. View all users/questions

## Database Inspection

**Android Studio Database Inspector:**
1. Run app on emulator/device
2. **View → Tool Windows → App Inspection**
3. Select **Database Inspector** tab
4. Explore tables: users, questions, answers, etc.

**Query Examples:**
```sql
-- Check user coins
SELECT name, coins, reputation FROM users;

-- View all questions
SELECT title, coin_reward, is_urgent FROM questions;

-- See answers with ratings
SELECT content, rating FROM answers WHERE rating IS NOT NULL;
```

## Clear Data (Reset)

To reset and re-seed:

1. **App Settings → Storage → Clear Data**
2. Or uninstall/reinstall app
3. DataSeeder will auto-populate on next launch

## Notes

- Seeding is **idempotent** - safe to call multiple times
- Only seeds if `test@kuet.ac.bd` doesn't exist
- All passwords are BCrypt hashed
- Timestamps are relative (2 hours ago, 1 day ago, etc.)
- Questions sorted by urgent first, then newest

---

**Pro Tip:** Use different accounts to test different user roles and interaction flows!
