package com.studymatch.model;

/** Đánh giá sau buổi học . */
public class SessionFeedback {
    public String sessionId;
    public String userId;
    /** 1..5 sao. */
    public int rating;
    public String comment;
    public long createdAt;

    public SessionFeedback() {
    }

    public SessionFeedback(String sessionId, String userId, int rating, String comment, long createdAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }
}
