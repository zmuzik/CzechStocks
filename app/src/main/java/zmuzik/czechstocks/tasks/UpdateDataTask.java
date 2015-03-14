package zmuzik.czechstocks.tasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.List;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.Dividend;
import zmuzik.czechstocks.dao.DividendDao;
import zmuzik.czechstocks.dao.HistoricalQuote;
import zmuzik.czechstocks.dao.HistoricalQuoteDao;
import zmuzik.czechstocks.dao.StockDetail;
import zmuzik.czechstocks.dao.StockDetailDao;
import zmuzik.czechstocks.dao.TodaysQuote;
import zmuzik.czechstocks.events.UpdateFinishedEvent;
import zmuzik.czechstocks.events.UpdateStartedEvent;
import zmuzik.czechstocks.helpers.PrefsHelper;
import zmuzik.czechstocks.utils.TimeUtils;

public class UpdateDataTask extends AsyncTask {

    private final String TAG = this.getClass().getSimpleName();
    private boolean currentDataUpdated = false;
    private boolean historyUpdated = false;
    private boolean stockInfoUpdated = false;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        App.getBus().post(new UpdateStartedEvent());
    }

    @Override
    protected Object doInBackground(Object... params) {
        if (!App.get().isOnline()) return null;
        try {
            currentDataUpdated = updateCurrentQuotes();
            currentDataUpdated = currentDataUpdated | updateTodaysData();
            if (PrefsHelper.get().isTimeToUpdateHistorical()) {
                historyUpdated = updateHistoricalData();
                stockInfoUpdated = updateStockDetails();
                stockInfoUpdated = stockInfoUpdated | updateDividends();
                PrefsHelper.get().setLastHistUpdateTime(TimeUtils.getNow());
            }

            if (currentDataUpdated || historyUpdated || stockInfoUpdated) {
                PrefsHelper.get().setLastUpdateTime(TimeUtils.getNow());
                App.getDaoSsn().clear();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Crashlytics.logException(e);
        }
        return null;
    }

    boolean updateCurrentQuotes() {
        List<CurrentQuote> currentQuotes = App.getServerApi().getCurrentQuotes();
        if (currentQuotes != null && currentQuotes.size() > 0) {
            App.getDaoSsn().getCurrentQuoteDao().insertOrReplaceInTx(currentQuotes);
            return true;
        }
        return false;
    }

    boolean updateTodaysData() {
        deleteOldTodaysQuotes(getCurrentDataStamp());
        long stamp = getLastTodaysQuoteStamp();
        List<TodaysQuote> todaysQuotes = App.getServerApi().getTodaysQuotes(stamp);
        if (todaysQuotes != null && todaysQuotes.size() > 0) {
            App.getDaoSsn().getTodaysQuoteDao().insertOrReplaceInTx(todaysQuotes);
            return true;
        }
        return false;
    }

    long getCurrentDataStamp() {
        long result = 0L;
        List<CurrentQuote> quotes = App.getDaoSsn().getCurrentQuoteDao().loadAll();
        if (quotes != null && quotes.size() > 0) {
            result = quotes.get(0).getStamp();
        }
        return result;
    }

    boolean updateHistoricalData() {
        long stamp = getLastHistoricalQuoteStamp();
        List<HistoricalQuote> quotes = App.getServerApi().getHistoricalQuotes(stamp);
        if (quotes != null && quotes.size() > 0) {
            HistoricalQuoteDao dao = App.getDaoSsn().getHistoricalQuoteDao();
            dao.insertOrReplaceInTx(quotes);
            return true;
        }
        return false;
    }

    boolean updateDividends() {
        List<Dividend> dividends = App.getServerApi().getDividends();
        if (dividends != null && dividends.size() > 0) {
            DividendDao dao = App.getDaoSsn().getDividendDao();
            dao.deleteAll();
            dao.insertOrReplaceInTx(dividends);
            return true;
        }
        return false;
    }

    boolean updateStockDetails() {
        List<StockDetail> stockDetails = App.getServerApi().getStockDetails();
        if (stockDetails != null && stockDetails.size() > 0) {
            StockDetailDao dao = App.getDaoSsn().getStockDetailDao();
            dao.deleteAll();
            dao.insertOrReplaceInTx(stockDetails);
            return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Object result) {
        App.getBus().post(new UpdateFinishedEvent(historyUpdated, stockInfoUpdated));
    }

    long getLastTodaysQuoteStamp() {
        long result = 0L;
        SQLiteDatabase db = App.getDaoSsn().getDatabase();
        Cursor cursor = db.rawQuery("select max(stamp) from todays_quote;", null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getLong(0);
        }
        cursor.close();
        return result;
    }

    long getLastHistoricalQuoteStamp() {
        long result = 0L;
        SQLiteDatabase db = App.getDaoSsn().getDatabase();
        Cursor cursor = db.rawQuery("select max(stamp) from historical_quote;", null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getLong(0);
        }
        cursor.close();
        return result;
    }

    /**
     * delete any todays quotes that whose timestap is from a different day than the supplied one
     *
     * @param incomingDataStamp
     */
    void deleteOldTodaysQuotes(long incomingDataStamp) {
        long dayBegin = incomingDataStamp - (incomingDataStamp % TimeUtils.ONE_DAY);
        long dayEnd = dayBegin + TimeUtils.ONE_DAY;
        SQLiteDatabase db = App.getDaoSsn().getDatabase();
        db.execSQL("delete from todays_quote where stamp < " + dayBegin + " or stamp > " + dayEnd + ";");
        App.getDaoSsn().clear();
    }
}