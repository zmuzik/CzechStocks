package zmuzik.czechstocks;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.Date;
import java.util.Locale;

import retrofit.RestAdapter;
import zmuzik.czechstocks.activities.MainActivity;
import zmuzik.czechstocks.dao.DaoMaster;
import zmuzik.czechstocks.dao.DaoSession;

public class App extends Application {

    private final String TAG = this.getClass().getSimpleName();
    private static App app;
    private Date mLastUpdated;

    private SQLiteDatabase mDb;
    private DaoSession mDaoSession;
    private MainActivity mMainActivity;
    ApiService mApiService;

    private Locale mLocale;

    public static App get() {
        return app;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "====================Initializing app====================");
        super.onCreate();
        app = this;

        if (isDebuggable()) {
            Log.d(TAG, "Debug build - Crashlytics disabled");
        } else {
            Log.d(TAG, "Release build - starting Crashlytics");
            Crashlytics.start(this);
        }

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(AppConf.SERVER_API_ROOT).build();
        mApiService = restAdapter.create(ApiService.class);

        initDb(AppConf.DB_NAME);
    }

    private void initDb(String dbName) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, dbName, null);

        mDb = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(mDb);
        mDaoSession = daoMaster.newSession();

        DbUtils dbUtils = DbUtils.getInstance(this);

        if (!dbUtils.isCurrentDbVersion()) {
            dbUtils.fillStockTable();
            dbUtils.fillDividendTable();
            dbUtils.fillStockInfoTable();
            dbUtils.fillTodaysQuoteTable();
            dbUtils.saveCurrentDbVersion();
        }
    }

    public boolean isDebuggable() {
        return 0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public void setMainActiviy(MainActivity a) {
        mMainActivity = a;
    }

    public MainActivity getMainActivity() {
        return mMainActivity;
    }

    public ApiService getApiService() {
        return mApiService;
    }
}
