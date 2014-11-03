package zmuzik.czechstocks;

import android.content.res.Resources;

import java.text.NumberFormat;
import java.util.Locale;

public class Utils {

    public static String getFormatedCurrencyAmount(double value) {
        Locale currentLocale = Locale.getDefault();
        Resources res = App.get().getResources();
        NumberFormat numberFormatter = NumberFormat.getNumberInstance(currentLocale);
        numberFormatter.setMaximumFractionDigits(2);
        numberFormatter.setMinimumFractionDigits(2);
        String formatedNumber = numberFormatter.format(value);
        return formatedNumber + " " + res.getString(R.string.currency);
    }

    public static String getFormatedPercentage(double value) {
        Locale currentLocale = Locale.getDefault();
        Resources res = App.get().getResources();
        NumberFormat numberFormatter = NumberFormat.getNumberInstance(currentLocale);
        numberFormatter.setMaximumFractionDigits(2);
        numberFormatter.setMinimumFractionDigits(2);
        String formatedNumber = numberFormatter.format(value);
        return formatedNumber + " " + "%";
    }
}
