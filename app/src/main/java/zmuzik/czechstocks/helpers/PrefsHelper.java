package zmuzik.czechstocks.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.utils.TimeUtils;

public class PrefsHelper {

    //LUT = last update time
    private static final String CURRENT_DATA_LUT = "CURRENT_DATA_LUT";
    private static final String HIST_DATA_LUT = "HIST_DATA_LUT";
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

    public void setCurrentDataLut(long timestamp) {
        getPrefs().edit().putLong(CURRENT_DATA_LUT, timestamp).commit();
    }

    public long getCurrentDataLut() {
        return getPrefs().getLong(CURRENT_DATA_LUT, 0);
    }

    public void setHistoricalDataLut(long timestamp) {
        getPrefs().edit().putLong(HIST_DATA_LUT, timestamp).commit();
    }

    public long getHistoricalDataLut() {
        return getPrefs().getLong(HIST_DATA_LUT, 0);
    }

    public void setGraphTimeFrame(int timeFrame) {
        getPrefs().edit().putInt(GRAPH_TIMEFRAME, timeFrame).apply();
    }

    public int getGraphTimeFrame() {
        return getPrefs().getInt(GRAPH_TIMEFRAME, 0);
    }

    public boolean isTimeToUpdateCurrent() {
        return getCurrentDataLut() + TimeUtils.ONE_MINUTE < TimeUtils.getNow();
    }

    public boolean isTimeToUpdateHistorical() {
        return getCurrentDataLut() + TimeUtils.FOUR_HOURS < TimeUtils.getNow();
    }
}
