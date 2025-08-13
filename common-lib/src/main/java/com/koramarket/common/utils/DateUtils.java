package com.koramarket.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // Conversion d’un LocalDate en String
    public static String format(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT));
    }

    // Parse une string en LocalDate
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT));
    }

    // Conversion Date <-> LocalDateTime
    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    // Ajouter/soustraire jours
    public static LocalDate plusDays(LocalDate date, int days) {
        return date.plusDays(days);
    }

    // Obtenir la date courante
    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
