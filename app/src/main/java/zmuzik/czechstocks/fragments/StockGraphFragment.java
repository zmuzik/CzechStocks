package zmuzik.czechstocks.fragments;

import android.app.Activity;
import android.app.Fragment;
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
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.activities.StockDetailActivity;
import zmuzik.czechstocks.dao.HistoricalQuote;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.local.GraphDateFormat;

public class StockGraphFragment extends Fragment {

    @InjectView(R.id.stockGraph)
    XYPlot stockGraph;

    Stock mStock;

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
