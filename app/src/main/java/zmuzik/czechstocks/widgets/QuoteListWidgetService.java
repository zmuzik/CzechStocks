package zmuzik.czechstocks.widgets;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.utils.Utils;

public class QuoteListWidgetService extends RemoteViewsService {

    public final String TAG = this.getClass().getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QuoteListRemoteViewsFactory(this.getApplicationContext());
    }
}

class QuoteListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    List<Stock> stocks;
    Context mContext;

    public QuoteListRemoteViewsFactory(Context context) {
        mContext = context;
    }

    @Override public void onCreate() {
        QueryBuilder qb = App.getDaoSsn().getStockDao().queryBuilder();
        qb.where(new WhereCondition.StringCondition("SHOW_IN_QUOTES_LIST = 1 AND ISIN IN " +
                "(SELECT ISIN FROM CURRENT_QUOTE) ORDER BY NAME COLLATE LOCALIZED ASC"));
        stocks = qb.list();
    }

    @Override public void onDataSetChanged() {
    }

    @Override public void onDestroy() {
    }

    @Override public int getCount() {
        return (stocks == null) ? 0 : stocks.size();
    }

    @Override public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote_list);
        Stock stock = stocks.get(position);
        CurrentQuote currentQuote = stock.getCurrentQuote();
        if (stock == null) return rv;

        rv.setTextViewText(R.id.stockNameTV, stock.getName());
        if (currentQuote != null) {
            double delta =  currentQuote.getDelta();
            rv.setTextViewText(R.id.stockDeltaTV, Utils.getFormattedPercentage(delta));
            rv.setTextViewText(R.id.stockPriceTV, Utils.getFormattedDecimal(currentQuote.getPrice()));
            rv.setTextColor(R.id.stockDeltaTV, getColor(delta));
        } else {
            rv.setTextViewText(R.id.stockDeltaTV, "");
            rv.setTextViewText(R.id.stockPriceTV, "");
        }
        rv.setOnClickFillInIntent(R.id.root, new Intent());
        return rv;
    }

    private int getColor(double delta) {
        return mContext.getResources().getColor((delta >= 0) ? R.color.lime : R.color.red);
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

