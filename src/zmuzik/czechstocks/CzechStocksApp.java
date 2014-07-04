package zmuzik.czechstocks;

import java.security.acl.LastOwnerException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import zmuzik.czechstocks.DaoMaster.DevOpenHelper;
import android.app.Application;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.style.LeadingMarginSpan;
import android.util.Log;

public class CzechStocksApp extends Application {

	private final String TAG = this.getClass().getSimpleName();
	private final String DB_NAME = "czech-stocks-db";
	private Date mLastUpdated;

	private SQLiteDatabase mDb;
	private DaoSession mDaoSession;
	private StockDao mStockDao;
	private StockListItemDao mStockListItemDao;
	private PortfolioItemDao mPortfolioItemDao;
	private MainActivity mMainActivity;

	private Locale mLocale;

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

		fillTableStockListItem();
		createStockListView();
		createPortfolioView();
		createTotalPortfolioView();
	}

	private void fillTableStockListItem() {
		if (isTableEmpty(mDb, "STOCK_LIST_ITEM")) {
			Log.i(TAG, "Filling STOCK_LIST_ITEM table with default values, because it's empty.");
			for (String isin : getResources().getStringArray(R.array.default_quotes_list)) {
				StockListItem item = new StockListItem(null, isin);
				mStockListItemDao.insert(item);
			}
		}
	}

	private void createStockListView() {
		StringBuffer sb = new StringBuffer();
		sb.append("create view if not exists STOCK_LIST as ");
		sb.append("select ");
		sb.append("sli._id, ");
		sb.append("s.name as NAME, ");
		sb.append("s.delta as DELTA, ");
		sb.append("s.price as PRICE ");
		sb.append("from stock s, ");
		sb.append("stock_list_item sli ");
		sb.append("where s.isin = sli.isin ");
		sb.append("order by s.name collate localized asc; ");

		try {
			mDb.execSQL(sb.toString());
		} catch (Exception e) {
			Log.e("Error while creating view STOCK_LIST", e.toString());
			e.printStackTrace();
		}
	}

	private void createPortfolioView() {
		StringBuffer sb = new StringBuffer();
		sb.append("create view if not exists PORTFOLIO as ");
		sb.append("select ");
		sb.append("p._id, ");
		sb.append("s.name as name, ");
		sb.append("s.price as current_price, ");
		sb.append("((s.price - p.price)/p.price)*100 as delta, ");
		sb.append("p.quantity as quantity, ");
		sb.append("p.price as original_price, ");
		sb.append("(s.price - p.price) * p.quantity as profit ");
		sb.append("from stock s, portfolio_item p ");
		sb.append("where s.isin = p.isin ");
		sb.append("order by s._id;");

		try {
			mDb.execSQL(sb.toString());
		} catch (Exception e) {
			Log.e("Error while creating view PORTFOLIO", e.toString());
			e.printStackTrace();
		}
	}

	private void createTotalPortfolioView() {
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
			mDb.execSQL(sb.toString());
		} catch (Exception e) {
			Log.e("Error while creating view PORTFOLIO", e.toString());
			e.printStackTrace();
		}
	}

	public void setLastUpdatedTime() {
		mLastUpdated = new Date();
	}

	protected String getLastUpdatedTime() {
		try {
			if (mLastUpdated == null) {
				return null;
			}
			SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			return formater.format(mLastUpdated);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected String getDataFromTime() {
		try {
			if (mDb == null || !mDb.isOpen()) {
				return null;
			}
			Cursor cursor = mDb.rawQuery("SELECT STAMP FROM STOCK WHERE _id = 1", null);
			if (!cursor.moveToFirst()) {
				return null;
			}
			SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			Date lastDataTime = new Date(Long.parseLong(cursor.getString(0)));
			return formater.format(lastDataTime);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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

	double getDoubleValue(String s) {
		if (s == null || "".equals(s)) {
			return (double) 0;
		}

		if (mLocale == null) {
			mLocale = getResources().getConfiguration().locale;
		}
		NumberFormat format = NumberFormat.getInstance(mLocale);
		Number number;
		try {
			number = format.parse(s);
		} catch (ParseException e) {
			number = Double.valueOf(s);
		}
		return number.doubleValue();
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
