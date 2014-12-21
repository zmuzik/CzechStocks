package zmuzik.czechstocks.utils;

import android.content.res.Resources;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;

public class Utils {

    public final static long ONE_MINUTE = 60;
    public final static long FIVE_MINUTES = ONE_MINUTE * 5;

    public static String getFormattedDecimal(double value, int decPlaces) {
        Locale currentLocale = Locale.getDefault();
        NumberFormat numberFormatter = NumberFormat.getNumberInstance(currentLocale);
        numberFormatter.setMaximumFractionDigits(decPlaces);
        numberFormatter.setMinimumFractionDigits(decPlaces);
        return numberFormatter.format(value);
    }

    public static String getFormattedDecimal(double value) {
        return getFormattedDecimal(value, 2);
    }

    public static String getFormattedCurrencyAmount(double value) {
        Resources res = App.get().getResources();
        return getFormattedDecimal(value) + " " + res.getString(R.string.currency);
    }

    public static String getFormattedPercentage(double value) {
        return getFormattedDecimal(value) + "%";
    }

    public static char getDecimalSeparator() {
        DecimalFormat format= (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols symbols=format.getDecimalFormatSymbols();
        return symbols.getDecimalSeparator();
    }

    public static long getNow() {
        return System.currentTimeMillis()/1000L;
    }

    public static String getFormattedDate(long timestamp) {
        if (timestamp == 0L) return "";
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
        return timeFormat.format(new Date(timestamp));
    }

    public static String getFormattedDateAndTime(long timestamp) {
        if (timestamp == 0L) return "";
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return timeFormat.format(new Date(timestamp * 1000L));
    }

    public static double getDoubleValue(String s) {
        if (s == null || "".equals(s)) {
            return (double) 0;
        }
        NumberFormat format = NumberFormat.getInstance(App.get().getResources().getConfiguration().locale);
        Number number;
        try {
            number = format.parse(s);
        } catch (ParseException e) {
            number = Double.valueOf(s);
        }
        return number.doubleValue();
    }
}
