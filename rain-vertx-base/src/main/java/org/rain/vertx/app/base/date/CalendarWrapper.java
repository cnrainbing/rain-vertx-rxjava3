package org.rain.vertx.app.base.date;

import java.util.Calendar;
import java.util.Date;

/**
 * Wrap around a {@link Calendar}, provide {@link DateTimeUtils} functionality with fluent-style.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.9.1.1
 */
public class CalendarWrapper {
    public static CalendarWrapper newInstance() {
        return new CalendarWrapper(Calendar.getInstance());
    }

    public static CalendarWrapper newInstance(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return new CalendarWrapper(cal);
    }

    public static CalendarWrapper newInstance(Calendar cal) {
        return new CalendarWrapper(cal);
    }

    private Calendar calendar;

    protected CalendarWrapper(Calendar calendar) {
        setCalendar(calendar);
    }

    protected CalendarWrapper setCalendar(Calendar calendar) {
        this.calendar = (Calendar) calendar.clone();
        return this;
    }

    private CalendarWrapper _setCalendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }

    public Calendar getCalendar() {
        return (Calendar) calendar.clone();
    }

    public Date getDate() {
        return calendar.getTime();
    }

    public CalendarWrapper startOfSecond() {
        return _setCalendar(DateTimeUtils.startOfSecond(calendar));
    }

    public CalendarWrapper startOfMinute() {
        return _setCalendar(DateTimeUtils.startOfMinute(calendar));
    }

    public CalendarWrapper startOfHour() {
        return _setCalendar(DateTimeUtils.startOfHour(calendar));
    }

    public CalendarWrapper startOfDay() {
        return _setCalendar(DateTimeUtils.startOfDay(calendar));
    }

    public CalendarWrapper startOfWeek() {
        return _setCalendar(DateTimeUtils.startOfWeek(calendar));
    }

    public CalendarWrapper startOfMonth() {
        return _setCalendar(DateTimeUtils.startOfMonth(calendar));
    }

    public CalendarWrapper startOfYear() {
        return _setCalendar(DateTimeUtils.startOfYear(calendar));
    }

    /**
     * @return
     * @since 0.9.2
     */
    public CalendarWrapper nextMillisecond() {
        return _setCalendar(DateTimeUtils.nextMillisecond(calendar));
    }

    public CalendarWrapper nextSecond() {
        return _setCalendar(DateTimeUtils.nextSecond(calendar));
    }

    public CalendarWrapper nextMinute() {
        return _setCalendar(DateTimeUtils.nextMinute(calendar));
    }

    public CalendarWrapper nextHour() {
        return _setCalendar(DateTimeUtils.nextHour(calendar));
    }

    public CalendarWrapper nextDay() {
        return _setCalendar(DateTimeUtils.nextDay(calendar));
    }

    public CalendarWrapper nextWeek() {
        return _setCalendar(DateTimeUtils.nextWeek(calendar));
    }

    public CalendarWrapper nextMonth() {
        return _setCalendar(DateTimeUtils.nextMonth(calendar));
    }

    public CalendarWrapper nextYear() {
        return _setCalendar(DateTimeUtils.nextYear(calendar));
    }

    /**
     * @return
     * @since 0.9.2
     */
    public CalendarWrapper prevMillisecond() {
        return _setCalendar(DateTimeUtils.prevMillisecond(calendar));
    }

    public CalendarWrapper prevSecond() {
        return _setCalendar(DateTimeUtils.prevSecond(calendar));
    }

    public CalendarWrapper prevMinute() {
        return _setCalendar(DateTimeUtils.prevMinute(calendar));
    }

    public CalendarWrapper prevHour() {
        return _setCalendar(DateTimeUtils.prevHour(calendar));
    }

    public CalendarWrapper prevDay() {
        return _setCalendar(DateTimeUtils.prevDay(calendar));
    }

    public CalendarWrapper prevWeek() {
        return _setCalendar(DateTimeUtils.prevWeek(calendar));
    }

    public CalendarWrapper prevMonth() {
        return _setCalendar(DateTimeUtils.prevMonth(calendar));
    }

    public CalendarWrapper prevYear() {
        return _setCalendar(DateTimeUtils.prevYear(calendar));
    }

    /**
     * @param amount
     * @return
     * @since 0.9.2
     */
    public CalendarWrapper addMilliseconds(int amount) {
        return _setCalendar(DateTimeUtils.addMilliseconds(calendar, amount));
    }

    /**
     * @param amount
     * @return
     * @since 0.9.2
     */
    public CalendarWrapper addSeconds(int amount) {
        return _setCalendar(DateTimeUtils.addSeconds(calendar, amount));
    }

    /**
     * @param amount
     * @return
     * @since 0.9.2
     */
    public CalendarWrapper addMinutes(int amount) {
        return _setCalendar(DateTimeUtils.addMinutes(calendar, amount));
    }

    /**
     * @param amount
     * @return
     * @since 0.9.2
     */
    public CalendarWrapper addHours(int amount) {
        return _setCalendar(DateTimeUtils.addHours(calendar, amount));
    }

    /**
     * @param amount
     * @return
     * @since 0.9.2
     */
    public CalendarWrapper addDays(int amount) {
        return _setCalendar(DateTimeUtils.addDays(calendar, amount));
    }

    /**
     * @param amount
     * @return
     * @since 0.9.2
     */
    public CalendarWrapper addWeeks(int amount) {
        return _setCalendar(DateTimeUtils.addWeeks(calendar, amount));
    }

    /**
     * @param amount
     * @return
     * @since 0.9.2
     */
    public CalendarWrapper addMonths(int amount) {
        return _setCalendar(DateTimeUtils.addMonths(calendar, amount));
    }

    /**
     * @param amount
     * @return
     * @since 0.9.2
     */
    public CalendarWrapper addYears(int amount) {
        return _setCalendar(DateTimeUtils.addYears(calendar, amount));
    }
}
