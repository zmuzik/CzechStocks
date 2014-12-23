package zmuzik.czechstocks.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.Plot;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.activities.StockDetailActivity;
import zmuzik.czechstocks.activities.StockGraphActivity;
import zmuzik.czechstocks.dao.HistoricalQuote;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.dao.TodaysQuote;
import zmuzik.czechstocks.helpers.PrefsHelper;
import zmuzik.czechstocks.local.GraphDateFormat;
import zmuzik.czechstocks.utils.TimeUtils;

public class StockGraphFragment extends Fragment {

    public static final int TIMEFRAME_1D = 0;
    public static final int TIMEFRAME_1M = 1;
    public static final int TIMEFRAME_6M = 2;
    public static final int TIMEFRAME_1Y = 3;
    public static final int TIMEFRAME_5Y = 4;
    public static final int TIMEFRAME_ALL = 5;

    @InjectView(R.id.stockGraph) XYPlot stockGraph;

    Stock mStock;
    ArrayList<Number> mPrices;
    ArrayList<Number> mDates;

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof StockDetailActivity) {
            mStock = ((StockDetailActivity) activity).getStock();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_graph, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override public void onResume() {
        super.onResume();
        updateGraph();
    }

    @Override public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    private void updateGraph() {
        prepareData();
        drawGraph();
    }

    void prepareData() {
        mPrices = new ArrayList<Number>();
        mDates = new ArrayList<Number>();
        int graphTimeFrame = PrefsHelper.get().getGraphTimeFrame();
        //todays quotes
        if (graphTimeFrame == TIMEFRAME_1D) {
            for (TodaysQuote historicalQuote : mStock.getTodaysQuoteList()) {
                mPrices.add(historicalQuote.getPrice());
                mDates.add(historicalQuote.getStamp());
            }
            return;
        }

        //historical quotes
        List<HistoricalQuote> quoteList = getHistoricalQuoteList(graphTimeFrame);
        for (HistoricalQuote historicalQuote : quoteList) {
            mPrices.add(historicalQuote.getPrice());
            mDates.add(historicalQuote.getStamp());
        }
    }

    List<HistoricalQuote> getHistoricalQuoteList(int graphTimeFrame) {
        long beginningStamp = 0;
        if (graphTimeFrame == TIMEFRAME_1M) {
            beginningStamp = TimeUtils.getXMonthsAgo(1);
        } else if (graphTimeFrame == TIMEFRAME_6M) {
            beginningStamp = TimeUtils.getXMonthsAgo(6);
        } else if (graphTimeFrame == TIMEFRAME_1Y) {
            beginningStamp = TimeUtils.getXYearsAgo(1);
        } else if (graphTimeFrame == TIMEFRAME_5Y) {
            beginningStamp = TimeUtils.getXYearsAgo(5);
        } else { //TIMEFRAME_ALL
            return mStock.getHistoricalQuoteList();
        }
        QueryBuilder qb = App.getDaoSsn().getHistoricalQuoteDao().queryBuilder();
        qb.where(new WhereCondition.StringCondition("STAMP >= " + beginningStamp));
        return qb.list();
    }

    void drawGraph() {
        SimpleXYSeries priceSeries = new SimpleXYSeries(mDates, mPrices, null);
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

    @OnClick(R.id.stockGraph) void onStockGraphClick() {
        if (getActivity() instanceof StockGraphActivity) return;
        Intent intent = new Intent(getActivity(), StockGraphActivity.class);
        intent.putExtra("isin", mStock.getIsin());
        startActivity(intent);
    }

    @OnClick(R.id.oneDayBtn) void onOneDayBtnClick() {
        PrefsHelper.get().setGraphTimeFrame(TIMEFRAME_1D);
        updateGraph();
    }

    @OnClick(R.id.oneMonthBtn) void onOneMonthBtnClick() {
        PrefsHelper.get().setGraphTimeFrame(TIMEFRAME_1M);
        updateGraph();
    }

    @OnClick(R.id.sixMonthsBtn) void onSixMonthsBtnClick() {
        PrefsHelper.get().setGraphTimeFrame(TIMEFRAME_6M);
        updateGraph();
    }

    @OnClick(R.id.oneYearBtn) void onOneYearBtnClick() {
        PrefsHelper.get().setGraphTimeFrame(TIMEFRAME_1Y);
        updateGraph();
    }

    @OnClick(R.id.fiveYearsBtn) void onFiveYearsBtnClick() {
        PrefsHelper.get().setGraphTimeFrame(TIMEFRAME_5Y);
        updateGraph();
    }

    @OnClick(R.id.allTimeBtn) void onAllTimeBtnClick() {
        PrefsHelper.get().setGraphTimeFrame(TIMEFRAME_ALL);
        updateGraph();
    }
}
