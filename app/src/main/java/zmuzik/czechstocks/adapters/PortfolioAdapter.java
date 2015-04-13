package zmuzik.czechstocks.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.PortfolioItem;
import zmuzik.czechstocks.utils.Utils;
import zmuzik.czechstocks.widgets.PortfolioWidgetProvider;

public class PortfolioAdapter extends ArrayAdapter<PortfolioItem>
        implements RemoteViewsService.RemoteViewsFactory {

    private final String TAG = this.getClass().getSimpleName();
    private int mAppWidgetId;

    public PortfolioAdapter(Context context, Intent intent) {
        super(context, R.layout.list_item_portfolio, App.getDaoSsn().getPortfolioItemDao().loadAll());
        mAppWidgetId = Integer.valueOf(intent.getData().getSchemeSpecificPart())
                - PortfolioWidgetProvider.randomNumber;
    }

    public PortfolioAdapter(Context context, List<PortfolioItem> objects) {
        super(context, R.layout.list_item_portfolio, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_portfolio, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        if (isTotalItem(position)) {
            renderTotalItem(holder);
        } else {
            renderNormalItem(holder, position);
        }
        return convertView;
    }

    private void renderNormalItem(ViewHolder holder, int position) {
        try {
            PortfolioItem portfolioItem = getItem(position);
            holder.stockNameTV.setText(portfolioItem.getStock().getName());
            holder.quantityTV.setText(getAmountString(portfolioItem.getQuantity()));
            holder.originalPriceTV.setText(" " + Utils.getFormattedCurrencyAmount(portfolioItem.getPrice()));
            holder.deltaTV.setText(Utils.getFormattedPercentage(getDelta(portfolioItem)));
            holder.profitTV.setText(Utils.getFormattedCurrencyAmount(getProfit(portfolioItem)));
            // set color
            if (getProfit(portfolioItem) >= 0) {
                holder.deltaTV.setTextColor(App.get().getResources().getColor(R.color.lime));
                holder.profitTV.setTextColor(App.get().getResources().getColor(R.color.lime));
            } else {
                holder.deltaTV.setTextColor(App.get().getResources().getColor(R.color.red));
                holder.profitTV.setTextColor(App.get().getResources().getColor(R.color.red));
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    private void renderTotalItem(ViewHolder holder) {
        double totalInvested = 0;
        double totalProfit = 0;
        for (int i = 0; i < super.getCount(); i++) {
            PortfolioItem portfolioItem = getItem(i);
            CurrentQuote currentQuote = portfolioItem.getStock().getCurrentQuote();
            if (currentQuote == null) return;
            totalInvested += portfolioItem.getPrice() * portfolioItem.getQuantity();
            totalProfit += (currentQuote.getPrice() - portfolioItem.getPrice()) * portfolioItem.getQuantity();
        }
        double perCentProfit = (totalProfit / totalInvested) * 100;
        holder.stockNameTV.setText(App.get().getResources().getString(R.string.total));
        holder.deltaTV.setText(Utils.getFormattedPercentage(perCentProfit));
        holder.profitTV.setText(Utils.getFormattedCurrencyAmount(totalProfit));
        String investedStr = App.get().getResources().getString(R.string.invested);
        holder.quantityTV.setText(investedStr + " " + Utils.getFormattedCurrencyAmount(totalInvested));
        holder.originalPriceTV.setText("");
        // set color
        if (totalProfit >= 0) {
            holder.deltaTV.setTextColor(App.get().getResources().getColor(R.color.lime));
            holder.profitTV.setTextColor(App.get().getResources().getColor(R.color.lime));
        } else {
            holder.deltaTV.setTextColor(App.get().getResources().getColor(R.color.red));
            holder.profitTV.setTextColor(App.get().getResources().getColor(R.color.red));
        }
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

    @Override public void onCreate() {

    }

    @Override public void onDataSetChanged() {

    }

    @Override public void onDestroy() {

    }

    @Override
    public int getCount() {
        int noSummaryCount = super.getCount();
        return (noSummaryCount > 0) ? noSummaryCount + 1 : 0;
    }

    @Override public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(getContext().getPackageName(), R.layout.list_item_portfolio);
        if (isTotalItem(position)) {
            return getTotalWidgetItem(rv);
        } else {
            return getNormalWidgetItem(rv, getItem(position));
        }
    }

    public RemoteViews getNormalWidgetItem(RemoteViews rv, PortfolioItem item) {
        rv.setTextViewText(R.id.stockNameTV, item.getStock().getName());
        rv.setTextViewText(R.id.quantityTV, getAmountString(item.getQuantity()));
        rv.setTextViewText(R.id.originalPriceTV, " " + Utils.getFormattedCurrencyAmount(item.getPrice()));
        rv.setTextViewText(R.id.deltaTV, Utils.getFormattedPercentage(getDelta(item)));
        rv.setTextViewText(R.id.profitTV, Utils.getFormattedCurrencyAmount(getProfit(item)));

        if (getProfit(item) >= 0) {
            rv.setTextColor(R.id.deltaTV, App.get().getResources().getColor(R.color.lime));
            rv.setTextColor(R.id.profitTV, App.get().getResources().getColor(R.color.lime));
        } else {
            rv.setTextColor(R.id.deltaTV, App.get().getResources().getColor(R.color.red));
            rv.setTextColor(R.id.profitTV, App.get().getResources().getColor(R.color.red));
        }

        return rv;
    }

    public RemoteViews getTotalWidgetItem(RemoteViews rv) {
        double totalInvested = 0;
        double totalProfit = 0;
        for (int i = 0; i < super.getCount(); i++) {
            PortfolioItem portfolioItem = getItem(i);
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

        if (totalProfit >= 0) {
            rv.setTextColor(R.id.deltaTV, App.get().getResources().getColor(R.color.lime));
            rv.setTextColor(R.id.profitTV, App.get().getResources().getColor(R.color.lime));
        } else {
            rv.setTextColor(R.id.deltaTV, App.get().getResources().getColor(R.color.red));
            rv.setTextColor(R.id.profitTV, App.get().getResources().getColor(R.color.red));
        }
        return rv;
    }

    @Override public RemoteViews getLoadingView() {
        return null;
    }

    private boolean isTotalItem(int position) {
        int count = getCount();
        return (count > 1) && (position == count - 1);
    }

    static class ViewHolder {
        @InjectView(R.id.stockNameTV) TextView stockNameTV;
        @InjectView(R.id.deltaTV) TextView deltaTV;
        @InjectView(R.id.quantityTV) TextView quantityTV;
        @InjectView(R.id.originalPriceTV) TextView originalPriceTV;
        @InjectView(R.id.profitTV) TextView profitTV;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}