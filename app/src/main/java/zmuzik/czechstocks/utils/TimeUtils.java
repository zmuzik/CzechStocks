package zmuzik.czechstocks.utils;

import java.util.Calendar;

public class TimeUtils {

    public final static long ONE_SECOND = 1000;
    public final static long ONE_MINUTE = 60 * ONE_SECOND;
    public final static long FIVE_MINUTES = ONE_MINUTE * 5;

    public static long getNow() {
        return System.currentTimeMillis();
    }

    public static Calendar getTodayCalendar() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today;
    }

    public static long getToday() {
        return getTodayCalendar().getTimeInMillis();
    }

    public static long getXMonthsAgo(int months) {
        Calendar today = getTodayCalendar();
        today.add(Calendar.MONTH, -months);
        return today.getTimeInMillis();
    }

    public static long getXYearsAgo(int years) {
        Calendar today = getTodayCalendar();
        today.add(Calendar.YEAR, -years);
        return today.getTimeInMillis();
    }
}
