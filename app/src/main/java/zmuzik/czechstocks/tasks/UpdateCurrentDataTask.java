package zmuzik.czechstocks.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

import retrofit.RetrofitError;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.activities.MainActivity;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.CurrentQuoteDao;
import zmuzik.czechstocks.helpers.PrefsHelper;
import zmuzik.czechstocks.utils.TimeUtils;

public class UpdateCurrentDataTask extends AsyncTask {

    private final String TAG = this.getClass().getSimpleName();
    private boolean mDownloadError = false;
    private WeakReference<MainActivity> activityWeakReference;

    public UpdateCurrentDataTask(MainActivity activity) {
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }

    @Override
    protected Object doInBackground(Object... params) {
        if (!App.get().isOnline()) return null;
        try {
            List<CurrentQuote> currentQuotes = App.getServerApi().getCurrentQuotes();
            CurrentQuoteDao dao = App.getDaoSsn().getCurrentQuoteDao();
            dao.insertOrReplaceInTx(currentQuotes);
            PrefsHelper.get().setCurrentQuotesLut(TimeUtils.getNow());
            if (currentQuotes != null && currentQuotes.size() > 0) {
                PrefsHelper.get().setCurrentQuotesTime(currentQuotes.get(0).getStamp());
            }
        } catch (RetrofitError e) {
            Log.e(TAG, e.toString());
            mDownloadError = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        MainActivity activity = activityWeakReference.get();
        if (activity != null) {
            if (mDownloadError) {
                Toast.makeText(App.get(), R.string.toas_check_net, Toast.LENGTH_LONG).show();
            }
            activity.loadData();
        }
    }
}