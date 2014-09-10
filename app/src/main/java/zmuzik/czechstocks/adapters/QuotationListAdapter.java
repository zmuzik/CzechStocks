package zmuzik.czechstocks.adapters;

import android.content.Context;
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
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.QuoteListItem;
import zmuzik.czechstocks.dao.Stock;

public class QuotationListAdapter extends ArrayAdapter<Stock> {

    private final String TAG = this.getClass().getSimpleName();

    App app;

    public QuotationListAdapter(Context context, List<Stock> objects) {
        super(context, R.layout.stocks_list_item, objects);
        app = (App) context.getApplicationContext();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.stocks_list_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        CurrentQuote item = getItem(position).getCurrentQuote();
        holder.stockNameTV.setText(item.getName());
        holder.stockDeltaTV.setText(""+item.getDelta());
        holder.stockPriceTV.setText(""+item.getPrice());

        return convertView;
    }

    static class ViewHolder {
        @InjectView(R.id.stockNameTV)   TextView stockNameTV;
        @InjectView(R.id.stockDeltaTV)  TextView stockDeltaTV;
        @InjectView(R.id.stockPriceTV)  TextView stockPriceTV;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}