package zmuzik.czechstocks.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import zmuzik.czechstocks.R;
import zmuzik.czechstocks.utils.DbUtils;

public class FillDbTablesTask extends AsyncTask<Void, Integer, Void>{

    private final String TAG = this.getClass().getSimpleName();

    WeakReference<Context> ctx;
    ProgressDialog progressDialog;

    public FillDbTablesTask(Context ctx) {
        this.ctx = new WeakReference<Context>(ctx);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (ctx.get() == null) return;
        Resources res = ctx.get().getResources();
        progressDialog = new ProgressDialog(ctx.get());
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setMessage(res.getString(R.string.initializing_db));
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        DbUtils dbUtils = DbUtils.getInstance();
        if (!dbUtils.isCurrentDbVersion()) {
            dbUtils.fillStockTable();
            onProgressUpdate(2);
            dbUtils.fillDividendTable();
            onProgressUpdate(4);
            dbUtils.fillStockDetailTable();
            onProgressUpdate(6);
            dbUtils.fillHistoricalQuoteTable(this);
            dbUtils.saveCurrentDbVersion();
        }
        return null;
    }

    public void setProgress(int progress) {
        publishProgress(progress);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progress = values[0] <= 100 ? values[0] : 100;
        progressDialog.setProgress(progress);
        progressDialog.setMax(100);
    }
}
