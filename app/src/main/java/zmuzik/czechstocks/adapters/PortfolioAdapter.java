package zmuzik.czechstocks.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.PortfolioItem;

public class PortfolioAdapter extends ArrayAdapter<PortfolioItem> {

    private final String TAG = this.getClass().getSimpleName();

    App app;

    public PortfolioAdapter(Context context, List<PortfolioItem> objects) {
        super(context, R.layout.portfolio_item, objects);
        app = (App) context.getApplicationContext();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.portfolio_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }


        if (isTotalItem(position)) { //"total" (summary) item
            holder.stockNameTV.setText(app.getResources().getString(R.string.total));
            holder.deltaTV.setText("0 %");
            holder.profitTV.setText("0 " + app.getResources().getString(R.string.currency));
        } else { //regular portfolio item
            PortfolioItem portfolioItem = getItem(position);
            CurrentQuote quote = portfolioItem.getStock().getCurrentQuote();
            holder.stockNameTV.setText(portfolioItem.getStock().getName());
            holder.quantityTV.setText(""+portfolioItem.getQuantity());
            holder.originalPriceTV.setText(""+portfolioItem.getPrice());
            double delta = (quote.getPrice() / portfolioItem.getPrice()) / 100;
            double profit = (quote.getPrice() - portfolioItem.getPrice()) * portfolioItem.getQuantity();

            NumberFormat nf = NumberFormat.getNumberInstance(app.getResources().getConfiguration().locale);
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);
            holder.deltaTV.setText(nf.format(delta));
            holder.profitTV.setText(nf.format(profit) +" "+ app.getResources().getString(R.string.currency));
        }

        return convertView;
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
        return (count > 1) && (position == count -1);
    }

    static class ViewHolder {
        @InjectView(R.id.stockNameTV)       TextView stockNameTV;
        @InjectView(R.id.deltaTV)           TextView deltaTV;
        @InjectView(R.id.quantityTV)        TextView quantityTV;
        @InjectView(R.id.originalPriceTV)   TextView originalPriceTV;
        @InjectView(R.id.profitTV)          TextView profitTV;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}