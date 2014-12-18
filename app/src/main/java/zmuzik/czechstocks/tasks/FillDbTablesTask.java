package zmuzik.czechstocks.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import zmuzik.czechstocks.utils.DbUtils;
import zmuzik.czechstocks.R;

public class FillDbTablesTask extends AsyncTask<Void, Integer, Void>{

    private final String TAG = this.getClass().getSimpleName();

    Context ctx;
    ProgressDialog progressDialog;

    public FillDbTablesTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setTitle(ctx.getResources().getString(R.string.please_wait));
        progressDialog.setMessage(ctx.getResources().getString(R.string.initializing_db));
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
            onProgressUpdate(new Integer(2));
            dbUtils.fillDividendTable();
            onProgressUpdate(new Integer(4));
            dbUtils.fillStockInfoTable();
            onProgressUpdate(new Integer(6));
            dbUtils.fillTodaysQuoteTable();
            onProgressUpdate(new Integer(8));
            dbUtils.fillHistoricalQuoteTable(this);
            dbUtils.saveCurrentDbVersion();
        }
        return null;
    }

    public void setProgress(int progress) {
        publishProgress(new Integer(progress));
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progress = values[0] <= 100 ? values[0] : 100;
        progressDialog.setProgress(progress);
        progressDialog.setMax(100);
    }
}
