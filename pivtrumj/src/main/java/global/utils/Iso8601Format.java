package global.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Iso8601Format extends SimpleDateFormat {
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private Iso8601Format(final String formatString) {
        super(formatString);

        setTimeZone(UTC);
    }

    public static DateFormat newTimeFormat() {
        return new Iso8601Format("HH:mm:ss");
    }

    public static DateFormat newDateFormat() {
        return new Iso8601Format("yyyy-MM-dd");
    }

    public static DateFormat newDateTimeFormat() {
        return new Iso8601Format("yyyy-MM-dd HH:mm:ss");
    }

    public static String formatDateTime(final Date date) {
        return newDateTimeFormat().format(date);
    }

    public static Date parseDateTime(final String source) throws ParseException {
        return newDateTimeFormat().parse(source);
    }

    public static DateFormat newDateTimeFormatT() {
        return new Iso8601Format("yyyy-MM-dd'T'HH:mm:ss'Z'");
    }

    public static String formatDateTimeT(final Date date) {
        return newDateTimeFormatT().format(date);
    }

    public static Date parseDateTimeT(final String source) throws ParseException {
        return newDateTimeFormatT().parse(source);
    }
}