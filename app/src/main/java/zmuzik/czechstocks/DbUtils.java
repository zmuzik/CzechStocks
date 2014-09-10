package zmuzik.czechstocks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import zmuzik.czechstocks.dao.QuoteListItem;

public class DbUtils {

    private final String TAG = this.getClass().getSimpleName();
    private final String APP_VERSION_CODE_KEY = "appVersionCode";

    private static DbUtils instance = null;
    private App app;

    public DbUtils(App app) {
        this.app = app;
    }

    public static synchronized DbUtils getInstance(App app) {
        if (instance == null) {
            instance = new DbUtils(app);
        }
        return instance;
    }

    boolean isCurrentDbVersion() {
        SharedPreferences sharedPref = app.getSharedPreferences(app.getPackageName(), Context.MODE_PRIVATE);
        try {
            if (sharedPref.contains(APP_VERSION_CODE_KEY)) {
                int dbVersionCode = sharedPref.getInt(APP_VERSION_CODE_KEY, 0);
                int appVersionCode = app.getPackageManager().getPackageInfo(app.getPackageName(), 0).versionCode;
                return (dbVersionCode == appVersionCode);
            } else {
                return false;
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }
    }

    void saveCurrentDbVersion() {
        SharedPreferences sharedPref = app.getSharedPreferences(app.getPackageName(), Context.MODE_PRIVATE);
        try {
            int appVersionCode = app.getPackageManager().getPackageInfo(app.getPackageName(), 0).versionCode;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(APP_VERSION_CODE_KEY, appVersionCode);
            editor.commit();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    void fillTableQuoteListItem() {
        Log.i(TAG, "Filling QUOTE_LIST_ITEM table with default values");
        for (String isin : app.getResources().getStringArray(R.array.default_quotes_list)) {
            QuoteListItem item = new QuoteListItem(isin);
            app.getDaoSession().getQuoteListItemDao().insert(item);
        }
    }
}
