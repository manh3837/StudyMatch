package com.studymatch.data;

import com.studymatch.model.Subject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Danh mục cố định: môn học, mục tiêu, trình độ. */
public final class Catalog {

    public static final List<Subject> SUBJECTS = Collections.unmodifiableList(Arrays.asList(
            new Subject("android", "Lập trình Android"),
            new Subject("dsa", "Cấu trúc dữ liệu & giải thuật"),
            new Subject("db", "Cơ sở dữ liệu"),
            new Subject("web", "Lập trình Web"),
            new Subject("network", "Mạng máy tính"),
            new Subject("discrete", "Toán rời rạc"),
            new Subject("prob", "Xác suất thống kê"),
            new Subject("english", "Tiếng Anh chuyên ngành")
    ));

    public static final List<String> GOALS = Collections.unmodifiableList(Arrays.asList(
            "Ôn thi cuối kỳ",
            "Làm bài tập",
            "Luyện lập trình",
            "Làm đồ án",
            "Học trước bài mới"
    ));

    private static final String[] LEVELS = {"Chưa khai báo", "Cơ bản", "Trung bình", "Khá – Giỏi"};

    private Catalog() {
    }

    public static String subjectName(String id) {
        for (Subject s : SUBJECTS) {
            if (s.id.equals(id)) return s.name;
        }
        return id;
    }

    public static List<String> subjectNames(List<String> ids) {
        List<String> names = new ArrayList<>();
        if (ids == null) return names;
        for (String id : ids) names.add(subjectName(id));
        return names;
    }

    public static String levelLabel(int level) {
        return level >= 0 && level < LEVELS.length ? LEVELS[level] : LEVELS[0];
    }
}
