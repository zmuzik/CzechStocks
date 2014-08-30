package zmuzik.czechstocks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import zmuzik.czechstocks.dao.StockListItem;

public class DbUtils {

    private final String TAG = this.getClass().getSimpleName();
    private final String APP_VERSION_CODE = "APP_VERSION_CODE";

    private static DbUtils instance = null;
    private CzechStocksApp app;

    public DbUtils(CzechStocksApp app) {
        this.app = app;
    }

    public static synchronized DbUtils getInstance(CzechStocksApp app) {
        if (instance == null) {
            instance = new DbUtils(app);
        }
        return instance;
    }

    boolean isCurrentDbVersion() {
        SharedPreferences sharedPref = app.getSharedPreferences(app.getPackageName(), Context.MODE_PRIVATE);
        try {
            if (sharedPref.contains(APP_VERSION_CODE)) {
                int dbVersionCode = sharedPref.getInt(APP_VERSION_CODE, 0);
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
            editor.putInt(APP_VERSION_CODE, appVersionCode);
            editor.commit();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }


    boolean isTableEmpty(SQLiteDatabase db, String tableName) {
        if (tableName == null || db == null || !db.isOpen()) {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
        if (!cursor.moveToFirst()) {
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count == 0;
    }

    void fillTableStockListItem() {
        if (isTableEmpty(app.getDb(), "STOCK_LIST_ITEM")) {
            Log.i(TAG, "Filling STOCK_LIST_ITEM table with default values, because it's empty.");
            for (String isin : app.getResources().getStringArray(R.array.default_quotes_list)) {
                StockListItem item = new StockListItem(isin);
                app.getDaoSession().getStockListItemDao().insert(item);
            }
        }
    }

    void createStockListView() {
        Log.d(TAG, "Creating view stock_list");
        StringBuffer sb = new StringBuffer();
        sb.append("create view if not exists STOCK_LIST as ");
        sb.append("select ");
        sb.append("s.name as name, ");
        sb.append("s.delta as delta, ");
        sb.append("s.price as price ");
        sb.append("from current_trading_data s, ");
        sb.append("stock_list_item sli ");
        sb.append("where s.isin = sli.isin ");
        sb.append("order by s.name collate localized asc; ");

        try {
            app.getDb().execSQL(sb.toString());
        } catch (Exception e) {
            Log.e("Error while creating view STOCK_LIST", e.toString());
            Crashlytics.logException(e);
        }
    }

    void createPortfolioView() {
        Log.d(TAG, "Creating view portfolio");
        StringBuffer sb = new StringBuffer();
        sb.append("create view if not exists PORTFOLIO as ");
        sb.append("select ");
        sb.append("rowid as _id, ");
        sb.append("s.name as name, ");
        sb.append("s.price as current_price, ");
        sb.append("((s.price - p.price)/p.price)*100 as delta, ");
        sb.append("p.quantity as quantity, ");
        sb.append("p.price as original_price, ");
        sb.append("(s.price - p.price) * p.quantity as profit ");
        sb.append("from current_trading_data s, portfolio_item p ");
        sb.append("where s.isin = p.isin;");

        try {
            app.getDb().execSQL(sb.toString());
        } catch (Exception e) {
            Log.e("Error while creating view PORTFOLIO", e.toString());
            Crashlytics.logException(e);
        }
    }

    void createTotalPortfolioView() {
        Log.d(TAG, "Creating view total_portfolio");
        StringBuffer sb = new StringBuffer();
        sb.append("create view if not exists TOTAL_PORTFOLIO as ");
        sb.append("select _id, name, current_price, delta, quantity, original_price, profit from ( ");
        sb.append("select _id, name, current_price, delta, quantity, original_price, profit from portfolio ");
        sb.append("union ");
        sb.append("select ");
        sb.append("max(_id) +1 as _id, ");
        sb.append("\"TOTAL\" as name, ");
        sb.append("0 as price, ");
        sb.append("sum(profit)/sum(quantity*original_price)*100 as delta, ");
        sb.append("0 as quantity, ");
        sb.append("0 as original_price, ");
        sb.append("sum(profit) as profit ");
        sb.append("from portfolio); ");

        try {
            app.getDb().execSQL(sb.toString());
        } catch (Exception e) {
            Log.e("Error while creating view PORTFOLIO", e.toString());
            Crashlytics.logException(e);
        }
    }
}
