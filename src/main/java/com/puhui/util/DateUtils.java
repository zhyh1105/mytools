package com.puhui.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class DateUtils {
    /**
     * @author zhuyuhang
     */
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd";
    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(DEFAULT_PATTERN);

    /**
     * 添加或者减小月份
     * 
     * @author zhuyuhang
     * @param date
     * @param amount
     * @return
     */
    public static Date addMonths(Date date, int amount) {
        return add(date, Calendar.MONTH, amount);
    }

    /**
     * 获取给定日期的月最后一天 如果是当前月就是当前天 yyyy-MM-dd
     * 
     * @author zhuyuhang
     * @param date
     * @return
     */
    public static String modifyToLastDayOfMonth(Date date) {
        return modifyToLastDayOfMonth(date, DEFAULT_PATTERN);
    }

    /**
     * 获取给定日期的月最后一天 如果是当前月就是当前天
     * 
     * @author zhuyuhang
     * @param date
     * @param pattern
     * @return
     */
    public static String modifyToLastDayOfMonth(Date date, String pattern) {
        if (formatDate(new Date(), "MM").equals(formatDate(date, "MM"))) {
            return formatDate(date, pattern);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return formatDate(calendar.getTime(), pattern);
    }

    /**
     * 获取给定日期的月第一天
     * 
     * @author zhuyuhang
     * @param date
     * @return
     */
    public static String modifyToFirstDayOfMonth(Date date) {
        return modifyToFirstDayOfMonth(date, DEFAULT_PATTERN);
    }

    /**
     * 获取给定日期的月第一天
     * 
     * @author zhuyuhang
     * @param date
     * @param pattern
     * @return
     */
    public static String modifyToFirstDayOfMonth(Date date, String pattern) {
        return formatDate(set(date, Calendar.DAY_OF_MONTH, 1), pattern);
    }

    public static Date add(Date date, int calendar_field, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(calendar_field, value);
        return calendar.getTime();
    }

    public static Date set(Date date, int calendar_field, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(calendar_field, value);
        return calendar.getTime();
    }

    /**
     * 格式化日期 yyyy-MM-dd
     * 
     * @author zhuyuhang
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        return formatDate(date, null);
    }

    /**
     * 格式化日期 如果 pattern 是空则按 yyyy-MM-dd
     * 
     * @author zhuyuhang
     * @param date
     * @param pattern
     * @return
     */
    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat df = getDf(pattern);
        return df.format(date);
    }

    /**
     * @author zhuyuhang
     * @param pattern
     * @return
     */
    private static SimpleDateFormat getDf(String pattern) {
        SimpleDateFormat df = DEFAULT_DATE_FORMAT;
        if (!StringUtils.isBlank(pattern)) {
            df = new SimpleDateFormat(pattern);
        }
        return df;
    }

    public static void main(String[] args) {
        for (int i = 1; i < 14; i++) {
            System.out.println(modifyToLastDayOfMonth(addMonths(new Date(), -i)));
        }
    }
}
