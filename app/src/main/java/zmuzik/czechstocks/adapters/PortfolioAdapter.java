package zmuzik.czechstocks.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import zmuzik.czechstocks.CzechStocksApp;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.QuoteListItem;

public class PortfolioAdapter extends ArrayAdapter<QuoteListItem> {

    CzechStocksApp app;

    public PortfolioAdapter(Context context, List<QuoteListItem> objects) {
        super(context, R.layout.portfolio_item, objects);
        app = (CzechStocksApp) context.getApplicationContext();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.portfolio_item, parent, false);



        return convertView;
    }
}