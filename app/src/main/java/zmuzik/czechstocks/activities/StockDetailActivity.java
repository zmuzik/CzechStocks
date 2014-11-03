package zmuzik.czechstocks.activities;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import butterknife.ButterKnife;
import butterknife.InjectView;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.Utils;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.dao.StockDao;


public class StockDetailActivity extends Activity {

    private final String TAG = this.getClass().getSimpleName();

    Stock mStock;
    String mIsin;

    @InjectView(R.id.lastPrice)
    TextView lastPrice;
    @InjectView(R.id.delta)
    TextView delta;
    @InjectView(R.id.pe)
    TextView pe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsin = getIntent().getStringExtra("isin");
        if (mIsin == null) {
            Crashlytics.log(Log.ERROR, TAG, "Invalid ISIN. Unable to open stock detail");
            finish();
        }
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        StockDao stockDao = App.get().getDaoSession().getStockDao();
        mStock = stockDao.load(mIsin);
        if (mStock == null) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to load stock from the db. ISIN = " + mIsin);
            finish();
        }
        setTitle(mStock.getName());

        updateBasicInfo();
    }

    private void updateBasicInfo() {
        CurrentQuote currentQuote = mStock.getCurrentQuote();
        Resources res = App.get().getResources();
        lastPrice.setText(Utils.getFormatedCurrencyAmount(currentQuote.getPrice()));
        delta.setText(Utils.getFormatedPercentage(currentQuote.getDelta()));
        if (currentQuote.getDelta() >= 0) {
            delta.setTextColor(res.getColor(R.color.green));
        } else {
            delta.setTextColor(res.getColor(R.color.red));
        }
    }
}
