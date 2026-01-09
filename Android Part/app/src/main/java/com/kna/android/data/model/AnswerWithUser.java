package com.kna.android.data.model;

import androidx.room.Embedded;

/**
 * Combined Answer with User information for displaying
 */
public class AnswerWithUser {
    
    @Embedded
    public Answer answer;
    
    @Embedded(prefix = "user_")
    public UserBasicInfo userInfo;
    
    public static class UserBasicInfo {
        public String name;
        public String department;
        public int academicYear;
        public int reputation;
    }
}
