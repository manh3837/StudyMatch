package com.studymatch.matching;

import com.studymatch.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/** Một khung giờ chung được đề xuất cho nhóm. */
public class SlotSuggestion {
    public int dayOfWeek;
    public int startMinute;
    public int endMinute;
    /** Những thành viên rảnh trong toàn bộ khung giờ này. */
    public List<String> userIds = new ArrayList<>();

    public SlotSuggestion(int dayOfWeek, int startMinute, int endMinute, List<String> userIds) {
        this.dayOfWeek = dayOfWeek;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.userIds = new ArrayList<>(userIds);
    }

    public int duration() {
        return endMinute - startMinute;
    }

    public int count() {
        return userIds.size();
    }

    @Override
    public String toString() {
        return Formatters.day(dayOfWeek) + ", " + Formatters.timeRange(startMinute, endMinute)
                + " (" + count() + " người)";
    }
}
