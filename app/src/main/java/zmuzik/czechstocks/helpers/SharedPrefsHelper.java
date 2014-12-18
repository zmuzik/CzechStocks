package zmuzik.czechstocks.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import zmuzik.czechstocks.App;

public class SharedPrefsHelper {

    //LUT = last update time
    private static final String CURRENT_QUOTE_TIME = "CURRENT_QUOTE_TIME";
    private static final String CURRENT_QUOTE_LUT = "CURRENT_QUOTE_LUT";
    private static final String DIVIDEND_LUT = "DIVIDEND_LUT";
    private static final String STOCK_INFO_LUT = "STOCK_INFO_LUT";

    private static SharedPrefsHelper instance = null;
    private final String PACKAGE_NAME;

    public SharedPrefsHelper() {
        PACKAGE_NAME = App.get().getPackageName();
    }

    public static synchronized SharedPrefsHelper get() {
        if (instance == null) {
            instance = new SharedPrefsHelper();
        }
        return instance;
    }

    private SharedPreferences getPrefs() {
        return App.get().getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
    }

    public void setCurrentQuoteTime(long timestamp) {
        getPrefs().edit().putLong(CURRENT_QUOTE_TIME, timestamp).apply();
    }

    public long getCurrentQuoteTime() {
        return getPrefs().getLong(CURRENT_QUOTE_TIME, 0);
    }

    public void setCurrentQuoteLastUpdateTime(long timestamp) {
        getPrefs().edit().putLong(CURRENT_QUOTE_LUT, timestamp).apply();
    }

    public long getCurrentQuoteLastUpdateTime() {
        return getPrefs().getLong(CURRENT_QUOTE_LUT, 0);
    }

    public void setDividendLastUpdateTime(long timestamp) {
        getPrefs().edit().putLong(DIVIDEND_LUT, timestamp).apply();
    }

    public long getDividendLastUpdateTime() {
        return getPrefs().getLong(DIVIDEND_LUT, 0);
    }

    public void setStockInfoLastUpdateTime(long timestamp) {
        getPrefs().edit().putLong(STOCK_INFO_LUT, timestamp).apply();
    }

    public long getStockInfoLastUpdateTime() {
        return getPrefs().getLong(STOCK_INFO_LUT, 0);
    }
}
