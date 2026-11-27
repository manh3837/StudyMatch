package com.studymatch.matching;

/** Bộ lọc tìm bạn học. */
public class MatchFilter {
    /** null = mọi môn chung với tôi. */
    public String subjectId;
    /** null = mục tiêu bất kỳ. Nếu có, đối phương phải có mục tiêu này. */
    public String goal;
    /** 0 = ngày bất kỳ; 1..7 = phải có giờ rảnh trùng vào ngày này. */
    public int dayOfWeek;
    /** true = chỉ giữ người cùng môn và có lịch trùng (khuyến nghị trong đề xuất). */
    public boolean requireEligible = true;
    /** Điểm tối thiểu 0..100. */
    public int minScore;

    public static MatchFilter defaults() {
        return new MatchFilter();
    }
}
