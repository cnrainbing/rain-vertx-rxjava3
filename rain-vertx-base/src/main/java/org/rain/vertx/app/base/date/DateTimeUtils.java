package org.rain.vertx.app.base.date;

import java.util.Calendar;
import java.util.Date;

/**
 * Helper class to work with {@link Date} and {@link Calendar}.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.3.1
 */
public class DateTimeUtils {
    /**
     * Create a new {@link Calendar} instance from a {@link Date} object.
     *
     * @param date
     * @return
     */
    public static Calendar createCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    /*----------------------------------------------------------------------*/

    /**
     * Sync values of calendar's fields.
     *
     * @param cal
     * @return the original supplied calendar instance
     * @since 0.9.2
     */
    private static Calendar sync(Calendar cal) {
        cal.getTimeInMillis();
        return cal;
    }

    /**
     * Calculate the next-millisecond point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.2
     */
    public static Calendar nextMillisecond(Calendar origin) {
        return addMilliseconds(origin, 1);
    }

    /**
     * Calculate the previous-millisecond point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.2
     */
    public static Calendar prevMillisecond(Calendar origin) {
        return addMilliseconds(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of milliseconds to the given {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addMilliseconds(Calendar origin, int value) {
        Calendar cal = sync((Calendar) origin.clone());
        cal.add(Calendar.MILLISECOND, value);
        return sync(cal);
    }

    /**
     * Calculate the next-millisecond point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.2
     */
    public static Calendar nextMillisecond(Date origin) {
        return addMilliseconds(origin, 1);
    }

    /**
     * Calculate the previous-millisecond point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.2
     */
    public static Calendar prevMillisecond(Date origin) {
        return addMilliseconds(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of milliseconds to the given {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addMilliseconds(Date origin, int value) {
        return addMilliseconds(createCalendar(origin), value);
    }

    /**
     * Calculate the start-of-second point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced. Note: if {@code origin} has
     * unsynced field, it may cause side-effect.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar startOfSecond(Calendar origin) {
        Calendar cal = (Calendar) origin.clone();
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Calculate the next-second point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar nextSecond(Calendar origin) {
        return addSeconds(origin, 1);
    }

    /**
     * Calculate the previous-second point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar prevSecond(Calendar origin) {
        return addSeconds(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of seconds to the given {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addSeconds(Calendar origin, int value) {
        Calendar cal = sync((Calendar) origin.clone());
        cal.add(Calendar.SECOND, value);
        return sync(cal);
    }

    /**
     * Calculate the start-of-second point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar startOfSecond(Date origin) {
        return startOfSecond(createCalendar(origin));
    }

    /**
     * Calculate the next-second point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar nextSecond(Date origin) {
        return addSeconds(origin, 1);
    }

    /**
     * Calculate the previous-second point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar prevSecond(Date origin) {
        return addSeconds(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of seconds to the given {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addSeconds(Date origin, int value) {
        return addSeconds(createCalendar(origin), value);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Calculate the start-of-minute point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced. Note: if {@code origin} has
     * unsynced field, it may cause side-effect.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar startOfMinute(Calendar origin) {
        Calendar cal = (Calendar) origin.clone();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        return cal;
    }

    /**
     * Calculate the next-minute point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar nextMinute(Calendar origin) {
        return addMinutes(origin, 1);
    }

    /**
     * Calculate the previous-minute point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar prevMinute(Calendar origin) {
        return addMinutes(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of minutes to the given {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addMinutes(Calendar origin, int value) {
        Calendar cal = sync((Calendar) origin.clone());
        cal.add(Calendar.MINUTE, value);
        return sync(cal);
    }

    /**
     * Calculate the start-of-minute point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar startOfMinute(Date origin) {
        return startOfMinute(createCalendar(origin));
    }

    /**
     * Calculate the next-minute point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar nextMinute(Date origin) {
        return addMinutes(origin, 1);
    }

    /**
     * Calculate the previous-minute point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar prevMinute(Date origin) {
        return addMinutes(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of minutes to the given {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addMinutes(Date origin, int value) {
        return addMinutes(createCalendar(origin), value);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Calculate the start-of-hour point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced. Note: if {@code origin} has
     * unsynced field, it may cause side-effect.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar startOfHour(Calendar origin) {
        Calendar cal = (Calendar) origin.clone();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        return cal;
    }

    /**
     * Calculate the next-hour point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar nextHour(Calendar origin) {
        return addHours(origin, 1);
    }

    /**
     * Calculate the previous-hour point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar prevHour(Calendar origin) {
        return addHours(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of hours to the given {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addHours(Calendar origin, int value) {
        Calendar cal = sync((Calendar) origin.clone());
        cal.add(Calendar.HOUR_OF_DAY, value);
        return sync(cal);
    }

    /**
     * Calculate the start-of-hour point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar startOfHour(Date origin) {
        return startOfHour(createCalendar(origin));
    }

    /**
     * Calculate the next-hour point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar nextHour(Date origin) {
        return addHours(origin, 1);
    }

    /**
     * Calculate the previous-hour point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar prevHour(Date origin) {
        return addHours(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of hours to the given {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addHours(Date origin, int value) {
        return addHours(createCalendar(origin), value);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Calculate the start-of-day point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced. Note: if {@code origin} has
     * unsynced field, it may cause side-effect.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar startOfDay(Calendar origin) {
        Calendar cal = (Calendar) origin.clone();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal;
    }

    /**
     * Calculate the next-date point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar nextDay(Calendar origin) {
        return addDays(origin, 1);
    }

    /**
     * Calculate the previous-day point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar prevDay(Calendar origin) {
        return addDays(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of days to the given {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addDays(Calendar origin, int value) {
        Calendar cal = sync((Calendar) origin.clone());
        cal.add(Calendar.DATE, value);
        return sync(cal);
    }

    /**
     * Calculate the start-of-day point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar startOfDay(Date origin) {
        return startOfDay(createCalendar(origin));
    }

    /**
     * Calculate the next-date point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar nextDay(Date origin) {
        return addDays(origin, 1);
    }

    /**
     * Calculate the previous-day point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar prevDay(Date origin) {
        return addDays(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of days to the given {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addDays(Date origin, int value) {
        return addDays(createCalendar(origin), value);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Calculate the start-of-week point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced. Note: if {@code origin} has
     * unsynced field, it may cause side-effect.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar startOfWeek(Calendar origin) {
        Calendar cal = (Calendar) origin.clone();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        return cal;
    }

    /**
     * Calculate the next-week point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar nextWeek(Calendar origin) {
        return addWeeks(origin, 1);
    }

    /**
     * Calculate the previous-week point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar prevWeek(Calendar origin) {
        return addWeeks(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of weeks to the given {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addWeeks(Calendar origin, int value) {
        Calendar cal = sync((Calendar) origin.clone());
        cal.add(Calendar.WEEK_OF_YEAR, value);
        return sync(cal);
    }

    /**
     * Calculate the start-of-week point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar startOfWeek(Date origin) {
        return startOfWeek(createCalendar(origin));
    }

    /**
     * Calculate the next-week point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar nextWeek(Date origin) {
        return addWeeks(origin, 1);
    }

    /**
     * Calculate the previous-week point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar prevWeek(Date origin) {
        return addWeeks(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of weeks to the given {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addWeeks(Date origin, int value) {
        return addWeeks(createCalendar(origin), value);
    }
    /*----------------------------------------------------------------------*/

    /**
     * Calculate the start-of-month point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced. Note: if {@code origin} has
     * unsynced field, it may cause side-effect.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar startOfMonth(Calendar origin) {
        Calendar cal = (Calendar) origin.clone();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal;
    }

    /**
     * Calculate the next-month point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar nextMonth(Calendar origin) {
        return addMonths(origin, 1);
    }

    /**
     * Calculate the previous-month point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar prevMonth(Calendar origin) {
        return addMonths(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of months to the given {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addMonths(Calendar origin, int value) {
        Calendar cal = sync((Calendar) origin.clone());
        cal.add(Calendar.MONTH, value);
        return sync(cal);
    }

    /**
     * Calculate the start-of-month point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar startOfMonth(Date origin) {
        return startOfMonth(createCalendar(origin));
    }

    /**
     * Calculate the next-month point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar nextMonth(Date origin) {
        return addMonths(origin, 1);
    }

    /**
     * Calculate the previous-month point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar prevMonth(Date origin) {
        return addMonths(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of months to the given {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addMonths(Date origin, int value) {
        return addMonths(createCalendar(origin), value);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Calculate the start-of-year point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced. Note: if {@code origin} has
     * unsynced field, it may cause side-effect.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar startOfYear(Calendar origin) {
        Calendar cal = (Calendar) origin.clone();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return cal;
    }

    /**
     * Calculate the next-year point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar nextYear(Calendar origin) {
        return addYears(origin, 1);
    }

    /**
     * Calculate the previous-year point of a supplied {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     */
    public static Calendar prevYear(Calendar origin) {
        return addYears(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of years to the given {@link Calendar}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addYears(Calendar origin, int value) {
        Calendar cal = sync((Calendar) origin.clone());
        cal.add(Calendar.YEAR, value);
        return sync(cal);
    }

    /**
     * Calculate the start-of-year point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields unsynced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar startOfYear(Date origin) {
        return startOfYear(createCalendar(origin));
    }

    /**
     * Calculate the next-year point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar nextYear(Date origin) {
        return addYears(origin, 1);
    }

    /**
     * Calculate the previous-year point of a supplied {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @return
     * @since 0.9.1.1
     */
    public static Calendar prevYear(Date origin) {
        return addYears(origin, -1);
    }

    /**
     * Add/Subtract the specified amount of years to the given {@link Date}.
     *
     * <p>
     * The returned {@link Calendar} has its fields synced.
     * </p>
     *
     * @param origin
     * @param value
     * @return
     * @since 0.9.2
     */
    public static Calendar addYears(Date origin, int value) {
        return addYears(createCalendar(origin), value);
    }
}
