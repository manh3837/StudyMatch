package com.studymatch.util;

import com.studymatch.model.TimeSlot;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Các hàm định dạng hiển thị (thuần Java, không phụ thuộc Android để dễ unit test). */
public final class Formatters {

    private static final String[] DAY_LABELS =
            {"", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
    private static final String[] DAY_SHORT =
            {"", "T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    private Formatters() {
    }

    public static String day(int dayOfWeek) {
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? DAY_LABELS[dayOfWeek] : "?";
    }

    public static String dayShort(int dayOfWeek) {
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? DAY_SHORT[dayOfWeek] : "?";
    }

    /** 1140 -> "19:00" */
    public static String time(int minuteOfDay) {
        return String.format(Locale.US, "%02d:%02d", minuteOfDay / 60, minuteOfDay % 60);
    }

    public static String timeRange(int start, int end) {
        return time(start) + "–" + time(end);
    }

    /** "Thứ 3, 19:00–21:00" */
    public static String slot(TimeSlot s) {
        return day(s.dayOfWeek) + ", " + timeRange(s.startMinute, s.endMinute);
    }

    /** 150 -> "2 giờ 30 phút" */
    public static String duration(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        if (h == 0) return m + " phút";
        if (m == 0) return h + " giờ";
        return h + " giờ " + m + " phút";
    }

    /** Calendar.MONDAY..SUNDAY -> 1..7 */
    public static int dayFromCalendar(int calendarDay) {
        return calendarDay == Calendar.SUNDAY ? 7 : calendarDay - 1;
    }

    /** 1..7 -> Calendar.MONDAY..SUNDAY */
    public static int calendarFromDay(int dayOfWeek) {
        return dayOfWeek == 7 ? Calendar.SUNDAY : dayOfWeek + 1;
    }

    /** "T3, 26/11/2026" */
    public static String date(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return dayShort(dayFromCalendar(c.get(Calendar.DAY_OF_WEEK))) + ", "
                + String.format(Locale.US, "%02d/%02d/%d",
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
    }

    /** "26/11" */
    public static String dayMonth(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return String.format(Locale.US, "%02d/%02d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1);
    }

    public static int minuteOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }

    public static int dayOfWeek(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return dayFromCalendar(c.get(Calendar.DAY_OF_WEEK));
    }

    /** "19:00–21:00" cho một buổi học. */
    public static String sessionTime(long start, int durationMinutes) {
        int s = minuteOfDay(start);
        int rawEnd = s + Math.max(0, durationMinutes);
        if (rawEnd <= 24 * 60) {
            return timeRange(s, rawEnd);
        }
        return time(s) + "–" + time(rawEnd % (24 * 60)) + " (+1)";
    }

    public static String join(List<String> items, String separator) {
        StringBuilder sb = new StringBuilder();
        if (items == null) return "";
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(separator);
            sb.append(items.get(i));
        }
        return sb.toString();
    }
}
