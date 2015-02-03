package zmuzik.czechstocks.utils;

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
import zmuzik.czechstocks.tasks.FillDbTablesTask;

public class DbUtils {

    private final String TAG = this.getClass().getSimpleName();

    private static DbUtils instance = null;

    public static synchronized DbUtils getInstance() {
        if (instance == null) {
            instance = new DbUtils();
        }
        return instance;
    }

    public boolean isDataFilled() {
        return App.getDaoSsn().getHistoricalQuoteDao().queryBuilder().limit(1).list().size() > 0;
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
                String[] items = line.split("\\|");
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
                String[] items = line.split("\\|");
                statement.clearBindings();
                long exDate = (items[3] == null || "".equals(items[3]) || "n/a".equals(items[3])) ? 0l : Long.parseLong(items[3]);
                long paymentDate = (items[4] == null || "".equals(items[4]) || "n/a".equals(items[4])) ? 0l : Long.parseLong(items[4]);
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

    public void fillStockDetailTable() {
        Log.d(TAG, "Deleting contents of table STOCK_DETAIL");
        StockDetailDao stockDetailDao = App.getDaoSsn().getStockDetailDao();
        stockDetailDao.deleteAll();
        Log.d(TAG, "Filling table STOCK_DETAIL with default data");
        SQLiteDatabase db = App.getDaoSsn().getDatabase();
        SQLiteStatement statement = db.compileStatement("INSERT INTO STOCK_DETAIL VALUES (?,?,?,?);");
        try {
            db.beginTransaction();
            Scanner scanner = new Scanner(App.get().getAssets().open("stock_detail.csv"));
            long counter = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] items = line.split("\\|");
                statement.clearBindings();
                statement.bindLong(1, counter++);
                statement.bindString(2, items[0]);
                statement.bindString(3, items[1]);
                statement.bindString(4, items[2]);
                statement.execute();
            }
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to initialize default data for STOCK_DETAIL table");
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public void fillHistoricalQuoteTable(FillDbTablesTask fillDbTablesTask) {
        Log.d(TAG, "Deleting contents of table HISTORICAL_QUOTE");
        HistoricalQuoteDao historicalQuoteDao = App.getDaoSsn().getHistoricalQuoteDao();
        historicalQuoteDao.deleteAll();
        Log.d(TAG, "Filling table HISTORICAL_QUOTE with default data");
        SQLiteDatabase db = App.getDaoSsn().getDatabase();
        SQLiteStatement statement = db.compileStatement("INSERT INTO HISTORICAL_QUOTE VALUES (?,?,?,?,?);");
        try {
            db.beginTransaction();
            Scanner scanner = new Scanner(App.get().getAssets().open("historical_quote.csv"));
            long counter = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] items = line.split("\\|");
                String isin = items[0];
                long timeDate = (items[1] == null || "".equals(items[1])) ? 0l : Long.parseLong(items[1]);
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
