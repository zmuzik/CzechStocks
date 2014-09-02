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
import zmuzik.czechstocks.CzechStocksApp;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.QuoteListItem;

public class QuotationListAdapter extends ArrayAdapter<QuoteListItem> {

    private final String TAG = this.getClass().getSimpleName();

    CzechStocksApp app;

    @InjectView(R.id.stockNameTV) TextView stockNameTV;
    @InjectView(R.id.stockDeltaTV) TextView stockDeltaTV;
    @InjectView(R.id.stockPriceTV) TextView stockPriceTV;


    public QuotationListAdapter(Context context, List<QuoteListItem> objects) {
        super(context, R.layout.stocks_list_item, objects);
        app = (CzechStocksApp) context.getApplicationContext();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.stocks_list_item, parent, false);
        ButterKnife.inject(this, convertView);

        CurrentQuote item = getItem(position).getCurrentQuote();
        stockNameTV.setText(item.getName());
        stockDeltaTV.setText(""+item.getDelta());
        stockPriceTV.setText(""+item.getPrice());

        return convertView;
    }
}