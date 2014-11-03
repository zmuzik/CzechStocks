package zmuzik.czechstocks.tasks;

import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.CurrentQuoteDao;

public class UpdateCurrentDataTask extends AsyncTask {

    private final String TAG = this.getClass().getSimpleName();
    private boolean mDownloadError = false;

    @Override
    protected Object doInBackground(Object... params) {
        List<CurrentQuote> currentQuotes = App.get().getApiService().getCurrentQuotes();
        CurrentQuoteDao dao = App.get().getDaoSession().getCurrentQuoteDao();
        dao.insertOrReplaceInTx(currentQuotes);
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (App.get().getMainActivity() != null) {
            App.get().getMainActivity().setStaticRefreshIcon();
            if (mDownloadError) {
                Toast.makeText(App.get(), R.string.toas_check_net, Toast.LENGTH_LONG).show();
            } else {
                App.get().getMainActivity().refreshFragments();
            }
        }
    }
}