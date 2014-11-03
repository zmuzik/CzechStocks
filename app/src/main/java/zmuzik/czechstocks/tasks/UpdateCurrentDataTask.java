package zmuzik.czechstocks.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.CurrentQuoteDao;

public class UpdateCurrentDataTask extends AsyncTask {

    private final String TAG = this.getClass().getSimpleName();
    App app;
    private boolean mDownloadError = false;

    public UpdateCurrentDataTask(Context context) {
        app = (App) context;
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