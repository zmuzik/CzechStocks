package zmuzik.czechstocks.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import zmuzik.czechstocks.App;

public class SharedPrefsHelper {

    //LUT = last update time
    private static final String CURRENT_QUOTE_TIME = "CURRENT_QUOTE_TIME";
    private static final String CURRENT_QUOTE_LUT = "CURRENT_QUOTE_LUT";
    private static final String TODAYS_QUOTE_LUT = "TODAYS_QUOTE_LUT";
    private static final String HISTORICAL_QUOTE_LUT = "HISTORICAL_QUOTE_LUT";
    private static final String DIVIDEND_LUT = "DIVIDEND_LUT";
    private static final String STOCK_INFO_LUT = "STOCK_INFO_LUT";

    private static SharedPrefsHelper instance = null;
    private final String PACKAGE_NAME;

    public SharedPrefsHelper() {
        PACKAGE_NAME = App.get().getPackageName();
    }

    public static synchronized SharedPrefsHelper getInstance() {
        if (instance == null) {
            instance = new SharedPrefsHelper();
        }
        return instance;
    }

    private SharedPreferences getSharedPrefs() {
        return App.get().getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
    }

    public void setCurrentQuoteTime(long timestamp) {
        SharedPreferences prefs = getSharedPrefs();
        prefs.edit().putLong(CURRENT_QUOTE_TIME, timestamp).apply();
    }

    public long getCurrentQuoteTime() {
        SharedPreferences prefs = getSharedPrefs();
        return prefs.getLong(CURRENT_QUOTE_TIME, 0);
    }

    public void setCurrentQuoteLastUpdateTime(long timestamp) {
        SharedPreferences prefs = getSharedPrefs();
        prefs.edit().putLong(CURRENT_QUOTE_LUT, timestamp).apply();
    }

    public long getCurrentQuoteLastUpdateTime() {
        SharedPreferences prefs = getSharedPrefs();
        return prefs.getLong(CURRENT_QUOTE_LUT, 0);
    }

    public void setTodaysQuoteLastUpdateTime(long timestamp) {
        SharedPreferences prefs = getSharedPrefs();
        prefs.edit().putLong(TODAYS_QUOTE_LUT, timestamp).apply();
    }

    public long getTodaysQuoteLastUpdateTime() {
        SharedPreferences prefs = getSharedPrefs();
        return prefs.getLong(TODAYS_QUOTE_LUT, 0);
    }

    public void setHistoricalQuoteLastUpdateTime(long timestamp) {
        SharedPreferences prefs = getSharedPrefs();
        prefs.edit().putLong(HISTORICAL_QUOTE_LUT, timestamp).apply();
    }

    public long getHistoricalQuoteLastUpdateTime() {
        SharedPreferences prefs = getSharedPrefs();
        return prefs.getLong(HISTORICAL_QUOTE_LUT, 0);
    }

    public void setDividendLastUpdateTime(long timestamp) {
        SharedPreferences prefs = getSharedPrefs();
        prefs.edit().putLong(DIVIDEND_LUT, timestamp).apply();
    }

    public long getDividendLastUpdateTime() {
        SharedPreferences prefs = getSharedPrefs();
        return prefs.getLong(DIVIDEND_LUT, 0);
    }

    public void setStockInfoLastUpdateTime(long timestamp) {
        SharedPreferences prefs = getSharedPrefs();
        prefs.edit().putLong(STOCK_INFO_LUT, timestamp).apply();
    }

    public long getStockInfoLastUpdateTime() {
        SharedPreferences prefs = getSharedPrefs();
        return prefs.getLong(STOCK_INFO_LUT, 0);
    }
}
