package com.studymatch.model;

/** Buổi học nhóm (collection "studySessions"). */
public class StudySession {
    public String id;
    public String groupId;
    public String title;
    /** Thời điểm bắt đầu (epoch millis). */
    public long startTime;
    public int durationMinutes;
    public String location;
    public String note;
    public String createdBy;

    public StudySession() {
    }

    public long endTime() {
        return startTime + durationMinutes * 60_000L;
    }

    public boolean hasStarted(long now) {
        return startTime <= now;
    }

    public boolean isFinished(long now) {
        return endTime() <= now;
    }
}
