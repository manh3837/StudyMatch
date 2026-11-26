package com.studymatch.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    public static final int LEVEL_BASIC = 1;
    public static final int LEVEL_MEDIUM = 2;
    public static final int LEVEL_GOOD = 3;

    public String id;
    public String name;
    public String email;
    public String major;
    public String bio;
    /** 1 = Cơ bản, 2 = Trung bình, 3 = Khá – Giỏi (0 = chưa khai báo). */
    public int level;
    public List<String> subjectIds = new ArrayList<>();
    public List<String> goals = new ArrayList<>();
    public List<TimeSlot> availability = new ArrayList<>();
    /** Quyền riêng tư: ẩn lịch chi tiết, người khác chỉ thấy tổng thời gian trùng. */
    public boolean hideSchedule;

    public User() {
    }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public boolean isProfileComplete() {
        return subjectIds != null && !subjectIds.isEmpty()
                && availability != null && !availability.isEmpty();
    }

    public String firstName() {
        if (name == null || name.trim().isEmpty()) return "bạn";
        String[] parts = name.trim().split("\\s+");
        return parts[parts.length - 1];
    }
}
