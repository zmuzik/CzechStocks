package zmuzik.czechstocks;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import zmuzik.czechstocks.adapters.QuotationListAdapter;

public class QuoteListFragment extends ListFragment {

    final String TAG = this.getClass().getSimpleName();
    App app;
    TextView mLastUpdateTime;
    TextView mDataFromTime;
    QuotationListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) this.getActivity().getApplication();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stocks_list_fragment, container, false);
        mLastUpdateTime = (TextView) view.findViewById(R.id.lastUpdatedValue);
        mDataFromTime = (TextView) view.findViewById(R.id.dataFromValue);
        return view;
    }

    public void refreshData() {
        QueryBuilder qb = app.getDaoSession().getStockDao().queryBuilder();
        qb.where(new WhereCondition.StringCondition("SHOW_IN_QUOTES_LIST = 1 AND ISIN IN " +
                "(SELECT ISIN FROM CURRENT_QUOTE) ORDER BY NAME COLLATE LOCALIZED ASC"));
        List items = qb.list();

        mAdapter = new QuotationListAdapter(app, items);
        setListAdapter(mAdapter);

        if (mLastUpdateTime != null) {
            mLastUpdateTime.setText(app.getLastUpdatedTime());
        }
        if (mDataFromTime != null) {
            mDataFromTime.setText(app.getDataFromTime());
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        Intent intent = new Intent(getActivity(), StockDetailActivity.class);
        intent.putExtra("isin", mAdapter.getItem(pos).getIsin());
        startActivity(intent);
    }
}