package zmuzik.czechstocks.activities;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.crashlytics.android.Crashlytics;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.AppConf;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.Utils;
import zmuzik.czechstocks.adapters.DividendListAdapter;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.Dividend;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.dao.StockDao;
import zmuzik.czechstocks.dao.StockInfo;
import zmuzik.czechstocks.dao.TodaysQuote;


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

    @InjectView(R.id.stockGraph)
    XYPlot stockGraph;


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
        updateGraph();
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
        QueryBuilder qb = App.get().getDaoSession().getDividendDao().queryBuilder();
        qb.where(new WhereCondition.StringCondition("ISIN = '" + mStock.getIsin()
                + "' ORDER BY PAYMENT_DATE COLLATE LOCALIZED DESC"));
        List<Dividend> dividends = qb.list();
        dividendsListView.setAdapter(new DividendListAdapter(this, dividends));
    }

    private void updateGraph() {
        // prepare data
        List<TodaysQuote> quoteList = mStock.getTodaysQuoteList();
        ArrayList<Number> prices = new ArrayList<Number>();
        ArrayList<Number> dates = new ArrayList<Number>();
        for (TodaysQuote todaysQuote : quoteList) {
            prices.add(todaysQuote.getPrice());
            dates.add(todaysQuote.getStamp().getTime());
        }
        SimpleXYSeries priceSeries = new SimpleXYSeries(dates, prices, null);

        //graph formatting
        LineAndPointFormatter priceFormat = new LineAndPointFormatter(
                Color.rgb(0, 0, 100),           // line color
                null,                           // point color
                Color.rgb(0, 0, 200), null);    // fill color
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.WHITE, Color.GREEN, Shader.TileMode.MIRROR));

        stockGraph.setDomainValueFormat(new HourlyDateFormat());
        stockGraph.addSeries(priceSeries, priceFormat);
        stockGraph.getLegendWidget().setVisible(false);
    }


    private class HourlyDateFormat extends Format {
        SimpleDateFormat dateFormat;

        public HourlyDateFormat() {
            TimeZone exchangeTimeZone = TimeZone.getTimeZone(AppConf.EXCHANGE_TIME_ZONE);
            dateFormat = new SimpleDateFormat("HH:mm");
            dateFormat.setTimeZone(exchangeTimeZone);
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            Date date = new Date(((Number) obj).longValue());
            return dateFormat.format(date, toAppendTo, pos);
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return null;
        }
    }
}
