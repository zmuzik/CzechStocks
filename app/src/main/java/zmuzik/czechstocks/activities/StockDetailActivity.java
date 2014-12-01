package zmuzik.czechstocks.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import butterknife.ButterKnife;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.dao.StockDao;


public class StockDetailActivity extends Activity {

    private final String TAG = this.getClass().getSimpleName();

    Stock mStock;
    String mIsin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsin = getIntent().getStringExtra("isin");
        if (mIsin == null) {
            Crashlytics.log(Log.ERROR, TAG, "Invalid ISIN. Unable to open stock detail");
            finish();
        }
        loadStockFromDb();
        setTitle(mStock.getName());
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.inject(this);
    }

    private void loadStockFromDb() {
        StockDao stockDao = App.get().getDaoSession().getStockDao();
        mStock = stockDao.load(mIsin);
        if (mStock == null) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to load stock from the db. ISIN = " + mIsin);
            finish();
        }
    }

    public Stock getStock() {
        return mStock;
    }
}
