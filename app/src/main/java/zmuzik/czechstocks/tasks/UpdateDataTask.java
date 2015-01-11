package zmuzik.czechstocks.tasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;

import java.util.List;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.TodaysQuote;
import zmuzik.czechstocks.dao.TodaysQuoteDao;
import zmuzik.czechstocks.events.UpdateFinishedEvent;
import zmuzik.czechstocks.events.UpdateStartedEvent;
import zmuzik.czechstocks.helpers.PrefsHelper;
import zmuzik.czechstocks.utils.TimeUtils;

public class UpdateDataTask extends AsyncTask {

    private final String TAG = this.getClass().getSimpleName();
    private boolean updateHistory = false;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        App.getBus().post(new UpdateStartedEvent());
    }

    @Override
    protected Object doInBackground(Object... params) {
        if (!App.get().isOnline()) return null;
        try {
            if (updateCurrentQuotes() | updateTodaysQuotes()) {
                PrefsHelper.get().setCurrentDataLut(TimeUtils.getNow());
                App.getDaoSsn().clear();
            }
        } catch (Exception e) {
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

    boolean updateTodaysQuotes() {
        long stamp = getLastTodaysQuoteStamp();
        List<TodaysQuote> todaysQuotes = App.getServerApi().getTodaysQuotes(stamp);
        if (todaysQuotes != null && todaysQuotes.size() > 0) {
            TodaysQuoteDao dao = App.getDaoSsn().getTodaysQuoteDao();
            if (isNewBusinessDay(stamp, todaysQuotes)) {
                dao.deleteAll();
            }
            dao.insertOrReplaceInTx(todaysQuotes);
            return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Object result) {
        App.getBus().post(new UpdateFinishedEvent(updateHistory));
    }

    long getLastTodaysQuoteStamp() {
        long result = 0L;
        SQLiteDatabase db = App.getDaoSsn().getDatabase();
        Cursor c = db.rawQuery("select max(stamp) from todays_quote;", null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            result = c.getLong(0);
        }
        c.close();
        return result;
    }

    boolean isNewBusinessDay(long localStamp, List<TodaysQuote> serverData) {
        if (serverData != null && serverData.size() > 0) {
            TodaysQuote quoteFromServer = serverData.get(0);
            return (localStamp + TimeUtils.TEN_HOURS) < quoteFromServer.getStamp();
        } else {
            return false;
        }
    }
}