package zmuzik.czechstocks.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.AppConf;
import zmuzik.czechstocks.utils.TimeUtils;

public class PrefsHelper {


    private static final String LAST_UPD_TIME = "lastUpdateTime";
    private static final String LAST_HIST_UPD_TIME = "lastHistUpdateTime";
    private static final String GRAPH_TIME_FRAME = "graphTimeFrame";

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

    public void setLastUpdateTime(long timestamp) {
        getPrefs().edit().putLong(LAST_UPD_TIME, timestamp).commit();
    }

    public long getLastUpdateTime() {
        return getPrefs().getLong(LAST_UPD_TIME, 0);
    }

    public void setLastHistUpdateTime(long timestamp) {
        getPrefs().edit().putLong(LAST_HIST_UPD_TIME, timestamp).commit();
    }

    public long getLastHistUpdateTime() {
        return getPrefs().getLong(LAST_HIST_UPD_TIME, 0);
    }


    public void setGraphTimeFrame(int timeFrame) {
        getPrefs().edit().putInt(GRAPH_TIME_FRAME, timeFrame).apply();
    }

    public int getGraphTimeFrame() {
        return getPrefs().getInt(GRAPH_TIME_FRAME, 0);
    }

    public boolean isTimeToUpdateCurrent() {
        return getLastUpdateTime() + AppConf.CURRENT_DATA_UPDATE_INTERVAL < TimeUtils.getNow();
    }

    public boolean isTimeToUpdateHistorical() {
        return getLastHistUpdateTime() + AppConf.HIST_DATA_UPDATE_INTERVAL < TimeUtils.getNow();
    }
}
