package zmuzik.czechstocks.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.util.Scanner;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.dao.DividendDao;
import zmuzik.czechstocks.dao.HistoricalQuoteDao;
import zmuzik.czechstocks.dao.StockDao;
import zmuzik.czechstocks.dao.StockDetailDao;
import zmuzik.czechstocks.dao.TodaysQuoteDao;
import zmuzik.czechstocks.tasks.FillDbTablesTask;

public class DbUtils {

    private final String TAG = this.getClass().getSimpleName();
    private final String APP_VERSION_CODE_KEY = "appVersionCode";

    private static DbUtils instance = null;

    public static synchronized DbUtils getInstance() {
        if (instance == null) {
            instance = new DbUtils();
        }
        return instance;
    }

    public boolean isCurrentDbVersion() {
        SharedPreferences sharedPref = App.get().getSharedPreferences(App.get().getPackageName(), Context.MODE_PRIVATE);
        try {
            if (sharedPref.contains(APP_VERSION_CODE_KEY)) {
                int dbVersionCode = sharedPref.getInt(APP_VERSION_CODE_KEY, 0);
                int appVersionCode = App.get().getPackageManager().getPackageInfo(App.get().getPackageName(), 0).versionCode;
                return (dbVersionCode == appVersionCode);
            } else {
                return false;
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }
    }

    public void saveCurrentDbVersion() {
        SharedPreferences sharedPref = App.get().getSharedPreferences(App.get().getPackageName(), Context.MODE_PRIVATE);
        try {
            int appVersionCode = App.get().getPackageManager().getPackageInfo(App.get().getPackageName(), 0).versionCode;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(APP_VERSION_CODE_KEY, appVersionCode);
            editor.commit();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public void fillStockTable() {
        Log.d(TAG, "Deleting contents of table STOCK");
        StockDao stockDao = App.getDaoSsn().getStockDao();
        stockDao.deleteAll();
        Log.d(TAG, "Filling table STOCK with default data");
        SQLiteDatabase db = App.getDaoSsn().getDatabase();
        SQLiteStatement statement = db.compileStatement("INSERT INTO STOCK VALUES (?,?,?);");
        try {
            db.beginTransaction();
            Scanner scanner = new Scanner(App.get().getAssets().open("stock.csv"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] items = line.split(";");
                statement.clearBindings();
                statement.bindString(1, items[0]);
                statement.bindString(2, items[2]);
                statement.bindLong(3, "Y".equals(items[3]) ? 1l : 0l);
                statement.execute();
            }
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to initialize default data for STOCK table");
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public void fillDividendTable() {
        Log.d(TAG, "Deleting contents of table DIVIDEND");
        DividendDao dividendDao = App.getDaoSsn().getDividendDao();
        dividendDao.deleteAll();
        Log.d(TAG, "Filling table DIVIDEND with default data");
        SQLiteDatabase db = App.getDaoSsn().getDatabase();
        SQLiteStatement statement = db.compileStatement("INSERT INTO DIVIDEND VALUES (?,?,?,?,?,?);");
        try {
            db.beginTransaction();
            Scanner scanner = new Scanner(App.get().getAssets().open("dividend.csv"));
            long counter = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] items = line.split(";");
                statement.clearBindings();
                long exDate = (items[3] == null || "".equals(items[3]) || "n/a".equals(items[3])) ? 0l : Long.parseLong(items[3]) * 1000;
                long paymentDate = (items[4] == null || "".equals(items[4]) || "n/a".equals(items[4])) ? 0l : Long.parseLong(items[4]) * 1000;
                statement.bindLong(1, counter++);
                statement.bindString(2, items[0]);
                statement.bindDouble(3, Double.parseDouble(items[1]));
                statement.bindString(4, items[2]);
                statement.bindDouble(5, exDate);
                statement.bindDouble(6, paymentDate);
                statement.execute();
            }
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to initialize default data for DIVIDEND table");
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public void fillTodaysQuoteTable() {
        Log.d(TAG, "Deleting contents of table TODAYS_QUOTE");
        TodaysQuoteDao todaysQuoteDao = App.getDaoSsn().getTodaysQuoteDao();
        todaysQuoteDao.deleteAll();
        Log.d(TAG, "Filling table TODAYS_QUOTE with default data");
        SQLiteDatabase db = App.getDaoSsn().getDatabase();
        SQLiteStatement statement = db.compileStatement("INSERT INTO TODAYS_QUOTE VALUES (?,?,?,?,?);");
        try {
            db.beginTransaction();
            Scanner scanner = new Scanner(App.get().getAssets().open("todays_quote.csv"));
            long counter = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] items = line.split(";");
                statement.clearBindings();
                long timeDate = (items[1] == null || "".equals(items[1])) ? 0l : Long.parseLong(items[1]) * 1000;
                double amount = Double.parseDouble(items[2]);
                double volume = Double.parseDouble(items[3]);
                statement.bindLong(1, counter++);
                statement.bindString(2, items[0]);
                statement.bindLong(3, timeDate);
                statement.bindDouble(4, amount);
                statement.bindDouble(5, volume);
                statement.execute();
            }
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to initialize default data for TODAYS_QUOTE table");
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public void fillStockInfoTable() {
        Log.d(TAG, "Deleting contents of table STOCK_INFO");
        StockDetailDao stockDetailDao = App.getDaoSsn().getStockDetailDao();
        stockDetailDao.deleteAll();
        Log.d(TAG, "Filling table STOCK_INFO with default data");
        SQLiteDatabase db = App.getDaoSsn().getDatabase();
        SQLiteStatement statement = db.compileStatement("INSERT INTO STOCK_INFO VALUES (?,?,?,?);");
        try {
            db.beginTransaction();
            Scanner scanner = new Scanner(App.get().getAssets().open("stock_info.csv"));
            long counter = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] items = line.split(";");
                statement.clearBindings();
                statement.bindLong(1, counter++);
                statement.bindString(2, items[0]);
                statement.bindString(3, items[1]);
                statement.bindString(4, items[2]);
                statement.execute();
            }
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to initialize default data for STOCK_INFO table");
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public void fillHistoricalQuoteTable(FillDbTablesTask fillDbTablesTask) {
        Log.d(TAG, "Deleting contents of table STOCK_INFO");
        HistoricalQuoteDao historicalQuoteDao = App.getDaoSsn().getHistoricalQuoteDao();
        historicalQuoteDao.deleteAll();
        Log.d(TAG, "Filling table STOCK_INFO with default data");
        SQLiteDatabase db = App.getDaoSsn().getDatabase();
        SQLiteStatement statement = db.compileStatement("INSERT INTO HISTORICAL_QUOTE VALUES (?,?,?,?,?);");
        try {
            db.beginTransaction();
            Scanner scanner = new Scanner(App.get().getAssets().open("historical_quote.csv"));
            long counter = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] items = line.split(";");
                String isin = items[0];
                long timeDate = (items[1] == null || "".equals(items[1])) ? 0l : Long.parseLong(items[1]) * 1000;
                double amount = Double.parseDouble(items[2]);
                double volume = Double.parseDouble(items[3]);
                statement.clearBindings();
                statement.bindLong(1, counter++);
                statement.bindString(2, isin);
                statement.bindLong(3, timeDate);
                statement.bindDouble(4, amount);
                statement.bindDouble(5, volume);
                statement.execute();
                // constant 580 makes 90 per cent from approx 52000 records in this table (rough estimation)
                if (fillDbTablesTask != null & (counter % 580) == 0) {
                    fillDbTablesTask.setProgress(10 + (int) (counter / 580));
                }
            }
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to initialize default data for HISTORICAL_QUOTE table");
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }
}
