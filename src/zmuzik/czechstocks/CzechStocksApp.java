package zmuzik.czechstocks;

import zmuzik.czechstocks.DaoMaster.DevOpenHelper;
import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CzechStocksApp extends Application {

	private final String TAG = this.getClass().getSimpleName();
	private final String DB_NAME = "czech-stocks-db";
	private SQLiteDatabase mDb;
	private DaoSession mDaoSession;
	private StockDao mStockDao;
	private StockListItemDao mStockListItemDao;
	private PortfolioItemDao mPortfolioItemDao;
	private MainActivity mMainActivity;

	@Override
	public void onCreate() {
		Log.i(TAG, "===Initializing app===");
		super.onCreate();
		initDb(DB_NAME);
	}

	private void initDb(String dbName) {
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "czech-stocks-db", null);

		mDb = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(mDb);
		mDaoSession = daoMaster.newSession();

		mStockDao = mDaoSession.getStockDao();
		mStockListItemDao = mDaoSession.getStockListItemDao();
		mPortfolioItemDao = mDaoSession.getPortfolioItemDao();

		if (isTableEmpty(mDb, "STOCK_LIST_ITEM")) {
			Log.i(TAG, "Filling STOCK_LIST_ITEM table with default values, because it's empty.");
			for (String isin : getResources().getStringArray(R.array.default_quotes_list)) {
				StockListItem item = new StockListItem(null, isin);
				mStockListItemDao.insert(item);
			}
		}
	}

	private boolean isTableEmpty(SQLiteDatabase db, String tableName) {
		if (tableName == null || db == null || !db.isOpen()) {
			return false;
		}
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM "+tableName, null);
		if (!cursor.moveToFirst()) {
			return false;
		}
		int count = cursor.getInt(0);
		cursor.close();
		return count == 0;
	}

	SQLiteDatabase getDb() {
		return mDb;
	}

	DaoSession getDaoSession() {
		return mDaoSession;
	}

	StockDao getStockDao() {
		return mStockDao;
	}

	StockListItemDao getStockListItemDao() {
		return mStockListItemDao;
	}

	PortfolioItemDao getPortfolioItemDao() {
		return mPortfolioItemDao;
	}
	
	void setMainActiviy(MainActivity a) {
		mMainActivity = a;
	}
	
	MainActivity getMainActivity() {
		return mMainActivity;
	}
}
