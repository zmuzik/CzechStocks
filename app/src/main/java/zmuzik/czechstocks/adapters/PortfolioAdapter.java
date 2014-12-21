package zmuzik.czechstocks.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.utils.Utils;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.PortfolioItem;

public class PortfolioAdapter extends ArrayAdapter<PortfolioItem> {

    private final String TAG = this.getClass().getSimpleName();

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
    }

    private void renderTotalItem(ViewHolder holder) {
        double totalInvested = 0;
        double totalProfit = 0;
        for (int i = 0; i < super.getCount(); i++) {
            PortfolioItem portfolioItem = getItem(i);
            CurrentQuote currentQuote = portfolioItem.getStock().getCurrentQuote();
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

    private int getCountNoSummary() {
        return super.getCount();
    }

    @Override
    public int getCount() {
        int noSummaryCount = getCountNoSummary();
        if (noSummaryCount > 0) {
            return noSummaryCount + 1;
        } else {
            return 0;
        }
    }

    private boolean isTotalItem(int position) {
        int count = getCount();
        return (count > 1) && (position == count - 1);
    }

    static class ViewHolder {
        @InjectView(R.id.stockNameTV)
        TextView stockNameTV;
        @InjectView(R.id.deltaTV)
        TextView deltaTV;
        @InjectView(R.id.quantityTV)
        TextView quantityTV;
        @InjectView(R.id.originalPriceTV)
        TextView originalPriceTV;
        @InjectView(R.id.profitTV)
        TextView profitTV;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}