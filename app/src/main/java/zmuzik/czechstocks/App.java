package zmuzik.czechstocks;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import retrofit.RestAdapter;
import zmuzik.czechstocks.dao.DaoMaster;
import zmuzik.czechstocks.dao.DaoSession;
import zmuzik.czechstocks.events.MainThreadBus;

public class App extends Application {

    private final String TAG = this.getClass().getSimpleName();
    private static final MainThreadBus BUS = new MainThreadBus();
    private static App mApp;
    private static DaoSession mDaoSession;
    private static ServerApi mServerApi;

    private SQLiteDatabase mDb;

    public static App get() {
        return mApp;
    }

    public static DaoSession getDaoSsn() {
        return mDaoSession;
    }

    public static ServerApi getServerApi() {
        return mServerApi;
    }

    public static MainThreadBus getBus() {
        return BUS;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "====================Initializing app====================");
        mApp = this;
        super.onCreate();
        initCrashlytics();
        initServerApi();
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

    private void initServerApi() {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(AppConf.SERVER_API_ROOT).build();
        mServerApi = restAdapter.create(ServerApi.class);
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

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public int getAppVersionCode() {
        try {
            return App.get().getPackageManager().getPackageInfo(App.get().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }
}
