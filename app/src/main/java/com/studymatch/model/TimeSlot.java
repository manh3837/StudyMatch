package com.studymatch.model;

import com.studymatch.util.Formatters;

import java.util.Objects;

/**
 * Một khung giờ rảnh trong tuần (bản ghi logic "Availability").
 * dayOfWeek: 1 = Thứ 2 ... 6 = Thứ 7, 7 = Chủ nhật.
 * startMinute / endMinute: số phút tính từ 00:00 (ví dụ 19:00 = 1140).
 */
public class TimeSlot {
    public int dayOfWeek;
    public int startMinute;
    public int endMinute;

    public TimeSlot() {
    }

    public TimeSlot(int dayOfWeek, int startMinute, int endMinute) {
        this.dayOfWeek = dayOfWeek;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    /** Tạo nhanh khung giờ theo giờ chẵn */
    public static TimeSlot of(int dayOfWeek, int startHour, int endHour) {
        return new TimeSlot(dayOfWeek, startHour * 60, endHour * 60);
    }

    public int duration() {
        return Math.max(0, endMinute - startMinute);
    }

    public boolean isValid() {
        return dayOfWeek >= 1 && dayOfWeek <= 7
                && startMinute >= 0 && endMinute <= 24 * 60
                && startMinute < endMinute;
    }

    /** Khung giờ này có bao trọn [start, end) của ngày day hay không. */
    public boolean covers(int day, int start, int end) {
        return dayOfWeek == day && startMinute <= start && endMinute >= end;
    }

    public TimeSlot copy() {
        return new TimeSlot(dayOfWeek, startMinute, endMinute);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot)) return false;
        TimeSlot t = (TimeSlot) o;
        return dayOfWeek == t.dayOfWeek && startMinute == t.startMinute && endMinute == t.endMinute;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek, startMinute, endMinute);
    }

    @Override
    public String toString() {
        return Formatters.slot(this);
    }
}
