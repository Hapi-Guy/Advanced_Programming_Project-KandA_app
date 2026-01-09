package com.kna.android.data.model;

import androidx.room.Embedded;

/**
 * Combined Question with User information for displaying in feed
 * Similar to desktop app's joined query results
 */
public class QuestionWithUser {
    
    @Embedded
    public Question question;
    
    @Embedded(prefix = "user_")
    public UserBasicInfo userInfo;
    
    public static class UserBasicInfo {
        public String name;
        public String department;
        public int academicYear;
    }
}
