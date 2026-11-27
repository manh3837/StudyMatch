package com.studymatch.util;

import java.util.Calendar;

public final class DateUtils {

    private DateUtils() {
    }

    /**
     * Thời điểm gần nhất (sau now) rơi vào thứ dayOfWeek (1..7) lúc minuteOfDay.
     * Dùng để biến một khung giờ rảnh hằng tuần thành ngày giờ cụ thể của buổi học.
     */
    public static long nextOccurrence(int dayOfWeek, int minuteOfDay, long now) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        c.set(Calendar.HOUR_OF_DAY, minuteOfDay / 60);
        c.set(Calendar.MINUTE, minuteOfDay % 60);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int today = Formatters.dayFromCalendar(c.get(Calendar.DAY_OF_WEEK));
        int diff = (dayOfWeek - today + 7) % 7;
        c.add(Calendar.DAY_OF_MONTH, diff);
        if (c.getTimeInMillis() <= now) {
            c.add(Calendar.DAY_OF_MONTH, 7);
        }
        return c.getTimeInMillis();
    }

    /** n ngày trước, lúc minuteOfDay. */
    public static long daysAgoAt(int days, int minuteOfDay, long now) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        c.add(Calendar.DAY_OF_MONTH, -days);
        c.set(Calendar.HOUR_OF_DAY, minuteOfDay / 60);
        c.set(Calendar.MINUTE, minuteOfDay % 60);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
