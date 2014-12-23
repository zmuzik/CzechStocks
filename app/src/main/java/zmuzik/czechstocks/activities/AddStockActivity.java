package zmuzik.czechstocks.activities;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.adapters.AddStockAdapter;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.dao.StockDao;


public class AddStockActivity extends ListActivity {

    private final String TAG = this.getClass().getSimpleName();

    @InjectView(R.id.okButton)
    Button okButton;

    ArrayList<Stock> stocks;
    AddStockAdapter addStockAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_stock_list);
        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // initialize the list adapter
        QueryBuilder qb = App.getDaoSsn().getStockDao().queryBuilder();
        qb.where(new WhereCondition.StringCondition("SHOW_IN_QUOTES_LIST != 1 ORDER BY NAME COLLATE LOCALIZED ASC"));
        List<Stock> allStocks = qb.list();

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
        StockDao stockDao = App.getDaoSsn().getStockDao();
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
