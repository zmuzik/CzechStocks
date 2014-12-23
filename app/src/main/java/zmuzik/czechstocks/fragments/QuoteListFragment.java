package zmuzik.czechstocks.fragments;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.activities.StockDetailActivity;
import zmuzik.czechstocks.adapters.QuotationListAdapter;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.helpers.PrefsHelper;
import zmuzik.czechstocks.utils.Utils;

public class QuoteListFragment extends ListFragment {

    final String TAG = this.getClass().getSimpleName();

    @InjectView(R.id.lastUpdatedValueTV) TextView lastUpdatedValueTV;
    @InjectView(R.id.dataFromValueTV) TextView dataFromValueTV;

    QuotationListAdapter mAdapter;

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stocks_list, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public void refreshData() {
        QueryBuilder qb = App.getDaoSsn().getStockDao().queryBuilder();
        qb.where(new WhereCondition.StringCondition("SHOW_IN_QUOTES_LIST = 1 AND ISIN IN " +
                "(SELECT ISIN FROM CURRENT_QUOTE) ORDER BY NAME COLLATE LOCALIZED ASC"));
        List items = qb.list();

        mAdapter = new QuotationListAdapter(App.get(), items);
        setListAdapter(mAdapter);
        if (lastUpdatedValueTV != null && dataFromValueTV != null) {
            lastUpdatedValueTV.setText(Utils.getFormattedDateAndTime(PrefsHelper.get().getCurrentQuotesLut()));
            dataFromValueTV.setText(Utils.getFormattedDateAndTime(PrefsHelper.get().getCurrentQuotesTime()));
        }

        //add listener for long click (for deleting dialog)
        getListView().setOnItemLongClickListener(new ListView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                onListItemLongClick(pos, mAdapter.getItem(pos).getName());
                return true;
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        Intent intent = new Intent(getActivity(), StockDetailActivity.class);
        intent.putExtra("isin", mAdapter.getItem(pos).getIsin());
        startActivity(intent);
    }

    public void onListItemLongClick(int pos, String quoteName) {
        final int position = pos;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Resources res = getResources();
        builder.setTitle(res.getString(R.string.delete_from_quotes_list_title));
        builder.setMessage(String.format(res.getString(R.string.do_you_want_to_delete_quote), quoteName));
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                Stock stock = mAdapter.getItem(position);
                stock.setShowInQuotesList(false);
                App.getDaoSsn().getStockDao().update(stock);
                refreshData();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}