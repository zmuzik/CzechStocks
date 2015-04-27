package zmuzik.czechstocks.widgets;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.PortfolioItem;
import zmuzik.czechstocks.utils.Utils;

public class PortfolioWidgetService extends RemoteViewsService {

    public final String TAG = this.getClass().getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new PortfolioRemoteViewsFactory(this.getApplicationContext());
    }
}

class PortfolioRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    Context mContext;
    List<PortfolioItem> mItems;

    public PortfolioRemoteViewsFactory(Context context) {
        mContext = context;
        mItems = App.getDaoSsn().getPortfolioItemDao().loadAll();
    }

    @Override public void onCreate() {
    }

    @Override public void onDataSetChanged() {
    }

    @Override public void onDestroy() {
    }

    @Override public int getCount() {
        int noSummaryCount = (mItems == null) ? 0 : mItems.size();
        return (noSummaryCount > 0) ? noSummaryCount + 1 : 0;
    }

    @Override public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_portfolio);
        if (isTotalItem(position)) {
            return getTotalWidgetItem(rv);
        } else {
            return getNormalWidgetItem(rv, mItems.get(position));
        }
    }

    private int getColor(double delta) {
        return mContext.getResources().getColor((delta >= 0) ? R.color.lime : R.color.red);
    }

    public RemoteViews getTotalWidgetItem(RemoteViews rv) {
        double totalInvested = 0;
        double totalProfit = 0;
        for (int i = 0; i < mItems.size(); i++) {
            PortfolioItem portfolioItem = mItems.get(i);
            CurrentQuote currentQuote = portfolioItem.getStock().getCurrentQuote();
            if (currentQuote == null) return null;
            totalInvested += portfolioItem.getPrice() * portfolioItem.getQuantity();
            totalProfit += (currentQuote.getPrice() - portfolioItem.getPrice()) * portfolioItem.getQuantity();
        }
        double perCentProfit = (totalProfit / totalInvested) * 100;
        String investedStr = App.get().getResources().getString(R.string.invested);

        rv.setTextViewText(R.id.stockNameTV, App.get().getResources().getString(R.string.total));
        rv.setTextViewText(R.id.quantityTV, investedStr + " " + Utils.getFormattedCurrencyAmount(totalInvested));
        rv.setTextViewText(R.id.originalPriceTV, "");
        rv.setTextViewText(R.id.deltaTV, Utils.getFormattedPercentage(perCentProfit));
        rv.setTextViewText(R.id.profitTV, Utils.getFormattedCurrencyAmount(totalProfit));

        rv.setTextColor(R.id.deltaTV, getColor(totalProfit));
        rv.setTextColor(R.id.profitTV, getColor(totalProfit));

        rv.setOnClickFillInIntent(R.id.root, new Intent());
        return rv;
    }


    public RemoteViews getNormalWidgetItem(RemoteViews rv, PortfolioItem item) {
        rv.setTextViewText(R.id.stockNameTV, item.getStock().getName());
        rv.setTextViewText(R.id.quantityTV, getAmountString(item.getQuantity()));
        rv.setTextViewText(R.id.originalPriceTV, " " + Utils.getFormattedCurrencyAmount(item.getPrice()));
        rv.setTextViewText(R.id.deltaTV, Utils.getFormattedPercentage(getDelta(item)));
        rv.setTextViewText(R.id.profitTV, Utils.getFormattedCurrencyAmount(getProfit(item)));

        rv.setTextColor(R.id.deltaTV, getColor(getProfit(item)));
        rv.setTextColor(R.id.profitTV, getColor(getProfit(item)));
        rv.setOnClickFillInIntent(R.id.root, new Intent());
        return rv;
    }

    private double getDelta(PortfolioItem portfolioItem) {
        CurrentQuote quote = portfolioItem.getStock().getCurrentQuote();
        return ((quote.getPrice() / portfolioItem.getPrice()) - 1) * 100;
    }

    private double getProfit(PortfolioItem portfolioItem) {
        CurrentQuote quote = portfolioItem.getStock().getCurrentQuote();
        return (quote.getPrice() - portfolioItem.getPrice()) * portfolioItem.getQuantity();
    }

    private String getAmountString(int amount) {
        Resources res = App.get().getResources();
        return res.getQuantityString(R.plurals.pieces_bought_at, amount, amount);
    }

    private boolean isTotalItem(int position) {
        int count = getCount();
        return (count > 1) && (position == count - 1);
    }

    @Override public RemoteViews getLoadingView() {
        return null;
    }

    @Override public int getViewTypeCount() {
        return 1;
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public boolean hasStableIds() {
        return false;
    }
}

