package zmuzik.czechstocks;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import retrofit.RestAdapter;
import zmuzik.czechstocks.activities.MainActivity;
import zmuzik.czechstocks.dao.DaoMaster;
import zmuzik.czechstocks.dao.DaoSession;

public class App extends Application {

    private final String TAG = this.getClass().getSimpleName();
    private static App mApp;
    private static DaoSession mDaoSession;
    private static Api mApi;

    private SQLiteDatabase mDb;
    private MainActivity mMainActivity;

    public static App get() {
        return mApp;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "====================Initializing app====================");
        mApp = this;
        super.onCreate();
        initCrashlytics();
        initRestApi();
        initDb();
    }

    private void initCrashlytics() {
        if (isDebuggable()) {
            Log.d(TAG, "Debug build - Crashlytics disabled");
        } else {
            Log.d(TAG, "Release build - starting Crashlytics");
            Crashlytics.start(this);
        }
    }

    private void initRestApi() {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(AppConf.SERVER_API_ROOT).build();
        mApi = restAdapter.create(Api.class);
    }

    private void initDb() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, AppConf.DB_NAME, null);
        mDb = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(mDb);
        mDaoSession = daoMaster.newSession();
    }

    public boolean isDebuggable() {
        return 0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
    }

    public static DaoSession getDaoSsn() {
        return mDaoSession;
    }

    public void setMainActiviy(MainActivity a) {
        mMainActivity = a;
    }

    public MainActivity getMainActivity() {
        return mMainActivity;
    }

    public static Api getApi() {
        return mApi;
    }
}
