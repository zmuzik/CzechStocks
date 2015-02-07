package zmuzik.czechstocks.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.PortfolioItem;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.utils.Utils;

public class AddPortfolioItemActivity extends ActionBarActivity {

    private final String TAG = this.getClass().getSimpleName();

    @InjectView(R.id.okButton) Button okButton;
    @InjectView(R.id.chooseStockSpinner) Spinner chooseStockSpinner;
    @InjectView(R.id.numberOfStocksET) EditText numberOfStocksET;
    @InjectView(R.id.averagePriceET) EditText averagePriceET;

    List<Stock> allStocks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        ButterKnife.inject(this);
        initSpinner();
    }

    int getLayout() {
        return R.layout.activity_add_portfolio_item;
    }

    void initSpinner() {
        QueryBuilder qb = App.getDaoSsn().getStockDao().queryBuilder();
        qb.where(new WhereCondition.StringCondition("isin not in (select isin from portfolio_item)"+
                " ORDER BY NAME COLLATE LOCALIZED ASC"));
        allStocks = qb.list();
        ArrayList<String> stockNames = new ArrayList<String>();
        for (Stock stock : allStocks) {
            stockNames.add(stock.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(App.get(), android.R.layout.simple_spinner_dropdown_item, stockNames);
        chooseStockSpinner.setAdapter(adapter);
    }

    @OnClick(R.id.okButton) public void onOkButtonClicked() {
        int quantity = 0;
        double price = 0d;
        Resources res = getResources();
        try {
            quantity = Integer.valueOf(numberOfStocksET.getText().toString());
        } catch (Exception e) {
            Toast.makeText(App.get(), res.getString(R.string.number_of_stocks_err), Toast.LENGTH_LONG).show();
            return;
        }
        try {
            price = Utils.getDoubleValue(averagePriceET.getText().toString());
        } catch (Exception e) {
            Toast.makeText(App.get(), res.getString(R.string.average_price_err), Toast.LENGTH_LONG).show();
            return;
        }

        if (quantity == 0) {
            Toast.makeText(App.get(), res.getString(R.string.number_of_stocks_err), Toast.LENGTH_LONG).show();
            return;
        }
        if (price == 0d) {
            Toast.makeText(App.get(), res.getString(R.string.average_price_err), Toast.LENGTH_LONG).show();
            return;
        }

        savePortfolioItem(getStock().getIsin(), quantity, price);
        finish();
    }

    Stock getStock() {
        return allStocks.get(chooseStockSpinner.getSelectedItemPosition());
    }

    void savePortfolioItem(String isin, int quantity, double price) {
        PortfolioItem portfolioItem = new PortfolioItem();
        portfolioItem.setIsin(isin);
        portfolioItem.setPrice(price);
        portfolioItem.setQuantity(quantity);
        App.getDaoSsn().getPortfolioItemDao().insert(portfolioItem);
    }
}
