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

import com.androidplot.Plot;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
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
import zmuzik.czechstocks.dao.HistoricalQuote;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.dao.StockDao;
import zmuzik.czechstocks.dao.StockInfo;
import zmuzik.czechstocks.dao.TodaysQuote;
import zmuzik.czechstocks.local.GraphDateFormat;


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

    private void updateGraph1() {
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

        stockGraph.setDomainValueFormat(new GraphDateFormat("HH:mm"));
        stockGraph.addSeries(priceSeries, priceFormat);
        stockGraph.getLegendWidget().setVisible(false);
    }

    private void updateGraph() {
        // prepare data
        List<HistoricalQuote> quoteList = mStock.getHistoricalQuoteList();
        ArrayList<Number> prices = new ArrayList<Number>();
        ArrayList<Number> dates = new ArrayList<Number>();
        for (HistoricalQuote historicalQuote : quoteList) {
            prices.add(historicalQuote.getPrice());
            dates.add(historicalQuote.getStamp().getTime());
        }
        SimpleXYSeries priceSeries = new SimpleXYSeries(dates, prices, null);

        //graph formatting
        LineAndPointFormatter priceFormat = new LineAndPointFormatter(
                Color.rgb(0, 0, 200),           // line color
                null,                           // point color
                Color.rgb(0, 0, 100), null);    // fill color
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.rgb(0, 0, 150), Color.rgb(0, 0, 100), Shader.TileMode.MIRROR));
        priceFormat.setFillPaint(lineFill);

        stockGraph.setDomainValueFormat(new GraphDateFormat(GraphDateFormat.MONTH_YEAR_FORMAT));
        stockGraph.addSeries(priceSeries, priceFormat);
        stockGraph.getLegendWidget().setVisible(false);
        stockGraph.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        stockGraph.setPlotMargins(0, 0, 0, 0);
        stockGraph.setPlotPadding(0, 0, 0, 0);
        stockGraph.setGridPadding(0, 10, 0, 0);
        stockGraph.getGraphWidget().setSize(new SizeMetrics(
                0, SizeLayoutType.FILL,
                0, SizeLayoutType.FILL));
        stockGraph.getBackgroundPaint().setColor(Color.TRANSPARENT);
        stockGraph.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        stockGraph.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
    }
}
