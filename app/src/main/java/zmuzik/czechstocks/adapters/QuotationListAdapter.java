package zmuzik.czechstocks.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import zmuzik.czechstocks.CzechStocksApp;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.CurrentTradingData;
import zmuzik.czechstocks.dao.QuotationListItem;

public class QuotationListAdapter extends ArrayAdapter<QuotationListItem> {

    CzechStocksApp app;

    public QuotationListAdapter(Context context, List<QuotationListItem> objects) {
        super(context, R.layout.stocks_list_item, objects);
        app = (CzechStocksApp) context.getApplicationContext();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.stocks_list_item, parent, false);

        TextView stockNameTV = (TextView)convertView.findViewById(R.id.stockNameTV);
        TextView stockDeltaTV = (TextView)convertView.findViewById(R.id.stockDeltaTV);
        TextView stockPriceTV = (TextView)convertView.findViewById(R.id.stockPriceTV);

        CurrentTradingData item = getItem(position).getCurrentTradingData();
        stockNameTV.setText(item.getName());
        stockDeltaTV.setText(""+item.getDelta());
        stockPriceTV.setText(""+item.getPrice());

        return convertView;
    }
}