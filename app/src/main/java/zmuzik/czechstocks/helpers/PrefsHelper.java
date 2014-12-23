package zmuzik.czechstocks.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import zmuzik.czechstocks.App;

public class PrefsHelper {

    //LUT = last update time
    private static final String CURRENT_QUOTE_TIME = "CURRENT_QUOTE_TIME";
    private static final String CURRENT_QUOTE_LUT = "CURRENT_QUOTE_LUT";
    private static final String DIVIDEND_LUT = "DIVIDEND_LUT";
    private static final String STOCK_INFO_LUT = "STOCK_INFO_LUT";
    private static final String GRAPH_TIMEFRAME = "GRAPH_TIMEFRAME";

    private static PrefsHelper instance = null;
    private final String PACKAGE_NAME;

    public PrefsHelper() {
        PACKAGE_NAME = App.get().getPackageName();
    }

    public static synchronized PrefsHelper get() {
        if (instance == null) {
            instance = new PrefsHelper();
        }
        return instance;
    }

    private SharedPreferences getPrefs() {
        return App.get().getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
    }

    public void setCurrentQuotesTime(long timestamp) {
        getPrefs().edit().putLong(CURRENT_QUOTE_TIME, timestamp).commit();
    }

    public long getCurrentQuotesTime() {
        return getPrefs().getLong(CURRENT_QUOTE_TIME, 0);
    }

    public void setCurrentQuotesLut(long timestamp) {
        getPrefs().edit().putLong(CURRENT_QUOTE_LUT, timestamp).commit();
    }

    public long getCurrentQuotesLut() {
        return getPrefs().getLong(CURRENT_QUOTE_LUT, 0);
    }

    public void setDividendsLut(long timestamp) {
        getPrefs().edit().putLong(DIVIDEND_LUT, timestamp).apply();
    }

    public long getDividendsLut() {
        return getPrefs().getLong(DIVIDEND_LUT, 0);
    }

    public void setStockDetailsLut(long timestamp) {
        getPrefs().edit().putLong(STOCK_INFO_LUT, timestamp).apply();
    }

    public long getStockDetailsLut() {
        return getPrefs().getLong(STOCK_INFO_LUT, 0);
    }

    public void setGraphTimeFrame(int timeFrame) {
        getPrefs().edit().putInt(GRAPH_TIMEFRAME, timeFrame).apply();
    }

    public int getGraphTimeFrame() {
        return getPrefs().getInt(GRAPH_TIMEFRAME, 0);
    }
}
