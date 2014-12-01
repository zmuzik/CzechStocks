package zmuzik.czechstocks.activities;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.adapters.AddStockAdapter;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.dao.StockDao;


public class AddStockActivity extends ListActivity {

    private final String TAG = this.getClass().getSimpleName();

    App app;

    @InjectView(R.id.okButton)
    Button okButton;

    ArrayList<Stock> stocks;
    AddStockAdapter addStockAdapter;

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
        StockDao stockDao = app.getDaoSsn().getStockDao();
        List<Stock> allStocks = stockDao.loadAll();
        stocks = new ArrayList<Stock>();
        for (Stock stock : allStocks) {
            if (!stock.getShowInQuotesList()) {
                stocks.add(stock);
            }
        }
        addStockAdapter = new AddStockAdapter(this, R.layout.list_item_edit_stock, stocks);
        setListAdapter(addStockAdapter);
    }

    @OnClick(R.id.okButton)
    public void onOkButtonClicked() {
        StockDao stockDao = app.getDaoSsn().getStockDao();
        //save the changed items
        for (int i = 0; i < addStockAdapter.getCount(); i++) {
            Stock stock = addStockAdapter.getItem(i);
            if (stock.getShowInQuotesList()) {
                stockDao.insertOrReplace(stock);
            }
        }
        finish();
    }
}
