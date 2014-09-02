package zmuzik.czechstocks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.CurrentQuoteDao;

public class UpdateDataTask extends AsyncTask {

    private final String TAG = this.getClass().getSimpleName();
    CzechStocksApp app;
    private boolean mDownloadError = false;

    UpdateDataTask(Context context) {
        app = (CzechStocksApp) context;
    }

    @Override
    protected Object doInBackground(Object... params) {
        List<CurrentQuote> currentQuotes = app.getApiService().getCurrentQuotes();
        CurrentQuoteDao dao = app.getDaoSession().getCurrentQuoteDao();
        dao.insertOrReplaceInTx(currentQuotes);
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (app != null && app.getMainActivity() != null) {
            app.getMainActivity().setStaticRefreshIcon();
            if (mDownloadError) {
                Toast.makeText(app, R.string.toas_check_net, Toast.LENGTH_LONG).show();
            } else {
                app.getMainActivity().refreshFragments();
            }
        }
    }
}