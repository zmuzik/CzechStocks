package zmuzik.czechstocks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

import zmuzik.czechstocks.dao.Dividend;
import zmuzik.czechstocks.dao.DividendDao;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.dao.StockDao;
import zmuzik.czechstocks.dao.StockInfo;
import zmuzik.czechstocks.dao.StockInfoDao;
import zmuzik.czechstocks.dao.TodaysQuote;
import zmuzik.czechstocks.dao.TodaysQuoteDao;

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

    void fillStockTable() {
        Log.i(TAG, "Filling stock table with default values");
        StockDao stockDao = app.getDaoSession().getStockDao();
        try {
            Scanner scanner = new Scanner(app.getAssets().open("stocks.csv"));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] items = line.split(";");

                String isin = items[0];
                String name = items[2];
                boolean isInQuotesList = "Y".equals(items[3]);

                Stock stock = new Stock(isin, name, isInQuotesList);
                stockDao.insert(stock);
            }
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to initialize default stocks list");
        }
    }

    public void fillDividendTable() {
        try {
            DividendDao dividendDao = app.getDaoSession().getDividendDao();
            Scanner scanner = new Scanner(app.getAssets().open("dividends.csv"));
            long counter = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] items = line.split(";");

                String isin = items[0];
                double amount = Double.parseDouble(items[1]);
                String currency = items[2];
                Date exDate = (items[3] == null || "".equals(items[3]) || "n/a".equals(items[3])) ? null : new Date(Long.parseLong(items[3])*1000);
                Date paymentDate = (items[4] == null || "".equals(items[4]) || "n/a".equals(items[4])) ? null : new Date(Long.parseLong(items[4])*1000);

                Dividend dividend = new Dividend(counter++, isin, amount, currency, exDate, paymentDate);
                dividendDao.insert(dividend);
            }
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to initialize default dividends table");
        }
    }

    public void fillTodaysQuoteTable() {
        try {
            TodaysQuoteDao todaysQuoteDao = app.getDaoSession().getTodaysQuoteDao();
            Scanner scanner = new Scanner(app.getAssets().open("todays_quote.csv"));
            long counter = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] items = line.split(";");
                String isin = items[0];
                Date timeDate = (items[1] == null || "".equals(items[1])) ? null : new Date(Long.parseLong(items[1])*1000);
                double amount = Double.parseDouble(items[2]);
                double volume = Double.parseDouble(items[3]);

                TodaysQuote todaysQuote = new TodaysQuote(counter++, isin, timeDate, amount, volume);
                todaysQuoteDao.insert(todaysQuote);
            }
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to initialize default dividends table");
        }
    }

    public void fillStockInfoTable() {
        try {
            StockInfoDao stockInfoDao = app.getDaoSession().getStockInfoDao();
            Scanner scanner = new Scanner(app.getAssets().open("stock_info.csv"));
            long counter = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] items = line.split(";");

                StockInfo stockInfo = new StockInfo(counter++, items[0], items[1], items[2]);
                stockInfoDao.insert(stockInfo);
            }
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to initialize default dividends table");
        }
    }

//    private void bulkInsertOneHundredRecords() {
//        String sql = "INSERT INTO "+ SAMPLE_TABLE_NAME +" VALUES (?,?,?);";
//        SQLiteStatement statement = sampleDB.compileStatement(sql);
//        sampleDB.beginTransaction();
//        for (int i = 0; i<100; i++) {
//            statement.clearBindings();
//            statement.bindLong(1, i);
//            statement.bindLong(2, i);
//            statement.bindLong(3, i*i);
//            statement.execute();
//        }
//        sampleDB.setTransactionSuccessful();
//        sampleDB.endTransaction();
//    }
}
