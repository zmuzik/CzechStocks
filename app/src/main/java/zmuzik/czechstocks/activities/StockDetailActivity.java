package zmuzik.czechstocks.activities;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.Utils;
import zmuzik.czechstocks.adapters.DividendListAdapter;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.Dividend;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.dao.StockDao;
import zmuzik.czechstocks.dao.StockInfo;


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
    @InjectView(R.id.dividendsListView)
    ListView dividendsListView;


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
        loadStockFromDb();
        updateBasicInfo();
        updateDividendsList();
    }

    private void loadStockFromDb() {
        StockDao stockDao = App.get().getDaoSession().getStockDao();
        mStock = stockDao.load(mIsin);
        if (mStock == null) {
            Crashlytics.log(Log.ERROR, TAG, "Unable to load stock from the db. ISIN = " + mIsin);
            finish();
        }
    }

    private void updateBasicInfo() {
        CurrentQuote currentQuote = mStock.getCurrentQuote();
        Resources res = App.get().getResources();
        setTitle(mStock.getName());
        lastPrice.setText(Utils.getFormatedCurrencyAmount(currentQuote.getPrice()));

        delta.setText(Utils.getFormatedPercentage(currentQuote.getDelta()));
        delta.setTextColor(res.getColor((currentQuote.getDelta() >= 0) ? R.color.lime : R.color.red));

        for (StockInfo stockInfo : mStock.getStockInfoList()) {
            if ("P/E".equals(stockInfo.getIndicator()) && stockInfo.getValue() != null) {
                pe.setText(stockInfo.getValue().replace('.', Utils.getDecimalSeparator()));
                break;
            }
        }
    }

    private void updateDividendsList() {
        //List<Dividend> dividends = mStock.getDividendList();

        QueryBuilder qb = App.get().getDaoSession().getDividendDao().queryBuilder();
        qb.where(new WhereCondition.StringCondition("ISIN = '" + mStock.getIsin()
                + "' ORDER BY PAYMENT_DATE COLLATE LOCALIZED DESC"));
        List<Dividend> dividends = qb.list();

        dividendsListView.setAdapter(new DividendListAdapter(this, dividends));
    }
}
