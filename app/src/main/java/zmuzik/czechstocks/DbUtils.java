package zmuzik.czechstocks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.dao.StockDao;

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
        Log.i(TAG, "Filling stock table with default values");
        StockDao stockDao = app.getDaoSession().getStockDao();
        try {
            InputStream in = app.getAssets().open("stocks.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ";");
                String isin = tokenizer.nextToken();
                tokenizer.nextToken(); // don't need this item (yet)
                String name = tokenizer.nextToken();
                boolean isInQuotesList = "Y".equals(tokenizer.nextToken());
                Stock stock = new Stock(isin, name, isInQuotesList);
                stockDao.insert(stock);
            }
            br.close();
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to initialize default stocks list");
        }
    }
}
