package zmuzik.czechstocks.activities;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.dao.StockDao;


public class AddStockActivity extends ListActivity {

    private final String TAG = this.getClass().getSimpleName();

    App app;

    @InjectView(R.id.okButton)
    Button okButton;

    ArrayList<Stock> stocks;
    EditStockListAdapter editStockListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_stock_list);
        app = (App) getApplication();
        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // initialize the list adapter
        StockDao stockDao = app.getDaoSession().getStockDao();
        List<Stock> allStocks = stockDao.loadAll();
        stocks = new ArrayList<Stock>();
        for (Stock stock : allStocks) {
            if (!stock.getShowInQuotesList()) {
                stocks.add(stock);
            }
        }
        editStockListAdapter = new EditStockListAdapter(this, R.layout.edit_stock_list_item, stocks);
        setListAdapter(editStockListAdapter);
    }

    @OnClick(R.id.okButton)
    public void onOkButtonClicked() {
        StockDao stockDao = app.getDaoSession().getStockDao();
        //save the changed items
        for (int i = 0; i < editStockListAdapter.getCount(); i++) {
            Stock stock = editStockListAdapter.getItem(i);
            if (stock.getShowInQuotesList()) {
                stockDao.insertOrReplace(stock);
            }
        }
        finish();
    }

    private class EditStockListAdapter extends ArrayAdapter<Stock> {

        public EditStockListAdapter(Context context, int resource, List<Stock> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.edit_stock_list_item, parent, false);
            }

            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            checkBox.setText(getItem(position).getName());
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    getItem(position).setShowInQuotesList(b);
                }
            });
            return convertView;
        }
    }
}
