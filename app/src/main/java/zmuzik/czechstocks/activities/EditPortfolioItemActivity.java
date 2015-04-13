package zmuzik.czechstocks.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.InjectView;
import butterknife.OnClick;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.PortfolioItem;
import zmuzik.czechstocks.dao.Stock;

public class EditPortfolioItemActivity extends AddPortfolioItemActivity {


    @InjectView(R.id.stockNameTV) TextView stockNameTV;
    @InjectView(R.id.numberOfStocksET) EditText numberOfStocksET;
    @InjectView(R.id.averagePriceET) EditText averagePriceET;

    Stock mStock;
    PortfolioItem mPortfolioItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String isin = getIntent().getStringExtra("isin");
        if (isin == null) finish();
        mStock = App.getDaoSsn().getStockDao().load(isin);
        mPortfolioItem = App.getDaoSsn().getPortfolioItemDao().load(isin);
        stockNameTV.setText(mStock.getName());
        numberOfStocksET.setText("" + mPortfolioItem.getQuantity());
        averagePriceET.setText("" + mPortfolioItem.getPrice());
    }

    @Override int getLayout() {
        return R.layout.activity_edit_portfolio_item;
    }

    //no spinner in this subclass
    @Override void initSpinner() {
    }

    @OnClick(R.id.deleteButton) public void onDeleteButtonClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Resources res = getResources();
        builder.setTitle(res.getString(R.string.delete_from_portfolio_title));
        builder.setMessage(String.format(res.getString(R.string.do_you_want_to_delete_portfolio_item), mStock.getName()));
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                App.getDaoSsn().getPortfolioItemDao().delete(mPortfolioItem);
                finish();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override Stock getStock() {
        return mStock;
    }

    @Override void savePortfolioItem(String isin, int quantity, double price) {
        mPortfolioItem.setIsin(isin);
        mPortfolioItem.setPrice(price);
        mPortfolioItem.setQuantity(quantity);
        App.getDaoSsn().getPortfolioItemDao().update(mPortfolioItem);
        App.get().updatePortfolioWidget();
    }
}
