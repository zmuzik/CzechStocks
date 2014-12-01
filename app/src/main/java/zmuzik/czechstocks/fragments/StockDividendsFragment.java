package zmuzik.czechstocks.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.activities.StockDetailActivity;
import zmuzik.czechstocks.adapters.DividendListAdapter;
import zmuzik.czechstocks.dao.Dividend;
import zmuzik.czechstocks.dao.Stock;

public class StockDividendsFragment extends Fragment {

    @InjectView(R.id.dividendsListView)
    ListView dividendsListView;

    Stock mStock;

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof StockDetailActivity) {
            mStock = ((StockDetailActivity) activity).getStock();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_dividends, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override public void onResume() {
        super.onResume();
        updateDividendsList();
    }

    @Override public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    private void updateDividendsList() {
        QueryBuilder qb = App.getDaoSsn().getDividendDao().queryBuilder();
        qb.where(new WhereCondition.StringCondition("ISIN = '" + mStock.getIsin()
                + "' ORDER BY PAYMENT_DATE COLLATE LOCALIZED DESC"));
        List<Dividend> dividends = qb.list();
        dividendsListView.setAdapter(new DividendListAdapter(App.get(), dividends));
    }
}
