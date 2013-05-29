package de.bennir.DVBViewerController.util;

/**
 * Thanks to following project for the source of this helper methods:
 * https://code.google.com/p/dvbviewer-controller/
 */


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtils {
    /**
     * Gets the float date.
     *
     * @param date
     *            the date
     * @return the float date
     * @author RayBa
     * @date 08.04.2012
     */
    public static String getFloatDate(Date date) {
        StringBuffer result = new StringBuffer();
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        long days = getDaysSinceDelphiNull(date);
        long hours = cal.get(Calendar.HOUR_OF_DAY);
        long minutesOfDay = hours * 60;
        long minutes = cal.get(Calendar.MINUTE);
        minutesOfDay = minutesOfDay + minutes;
        Double percentage = minutesOfDay / (24d * 60d);
        StringBuffer percentageString = new StringBuffer(percentage.toString());
        percentageString.replace(0, 2, "");
        result.append(days).append(".").append(percentageString);
        return result.toString();
    }

    /**
     * Gets the days since delphi null.
     *
     * @param date
     *            the date
     * @return the days since delphi null
     * @author RayBa
     * @date 08.04.2012
     */
    public static long getDaysSinceDelphiNull(Date date) {
        return getDifference(truncate(date), getBaseDate());
    }

    /**
     * Gets the base date.
     *
     * @return the base date
     * @author RayBa
     * @date 08.04.2012
     */
    public static Date getBaseDate() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 30);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.YEAR, 1899);

        return truncate(cal.getTime());
    }

    /**
     * Truncate.
     *
     * @param date the date
     * @return the date´
     * @author RayBa
     * @date 07.04.2013
     */
    public static Date truncate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        date = cal.getTime();
        return date;
    }

    /**
     * Gets the days between two dates.
     *
     * @param a the a
     * @param b the b
     * @return the difference
     * @author RayBa
     * @date 08.04.2012
     */
    public static long getDifference(Date a, Date b) {
        Calendar startCal = GregorianCalendar.getInstance();
        startCal.setTimeZone(TimeZone.getDefault());
        startCal.setTime(a);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        startCal.set(Calendar.SECOND, 0);

        Calendar endCal = GregorianCalendar.getInstance();
        endCal.setTimeZone(TimeZone.getDefault());
        endCal.setTime(b);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.MILLISECOND, 0);
        endCal.set(Calendar.SECOND, 0);

        long endL = endCal.getTimeInMillis() + endCal.getTimeZone().getOffset(endCal.getTimeInMillis());
        long startL = startCal.getTimeInMillis() + startCal.getTimeZone().getOffset(startCal.getTimeInMillis());
        return (startL - endL) / (1000 * 60 * 60 * 24);
    }
}
