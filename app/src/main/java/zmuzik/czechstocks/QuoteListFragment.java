package zmuzik.czechstocks;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import zmuzik.czechstocks.adapters.QuotationListAdapter;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.dao.StockDao;

public class QuoteListFragment extends ListFragment {

    final String TAG = this.getClass().getSimpleName();
    App app;
    TextView mLastUpdateTime;
    TextView mDataFromTime;
    QuotationListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) this.getActivity().getApplicationContext();
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
        qb.where(StockDao.Properties.ShowInQuotesList.eq(true));
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
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Resources res = getResources();
                final StockDao dao = app.getDaoSession().getStockDao();
                final Stock stock = dao.loadByRowId(arg3);
                String stockName = ((TextView) ((LinearLayout) arg1).getChildAt(0)).getText().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.remove_title);
                builder.setMessage(String.format(res.getString(R.string.remove_stock_from_quotes_list), stockName));
                builder.setCancelable(true);

                builder.setNegativeButton(res.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                builder.setPositiveButton(res.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dao.delete(stock);
                        app.getMainActivity().refreshFragments();
                        dialog.dismiss();
                    }
                });

                builder.show();
                return true;
            }
        });
    }
}