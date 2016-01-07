package zmuzik.czechstocks.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.activities.AddStockActivity;
import zmuzik.czechstocks.activities.StockDetailActivity;
import zmuzik.czechstocks.adapters.QuoteListAdapter;
import zmuzik.czechstocks.dao.Stock;
import zmuzik.czechstocks.events.CurrentDataUpdatedEvent;
import zmuzik.czechstocks.events.InternetNotFoundEvent;
import zmuzik.czechstocks.events.UpdateErrorEvent;
import zmuzik.czechstocks.helpers.PrefsHelper;
import zmuzik.czechstocks.tasks.UpdateDataTask;
import zmuzik.czechstocks.utils.Utils;

public class QuoteListFragment extends ListFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    final String TAG = this.getClass().getSimpleName();

    @InjectView(R.id.lastUpdatedValueTV) TextView lastUpdatedValueTV;
    @InjectView(R.id.dataFromValueTV) TextView dataFromValueTV;
    @InjectView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;

    QuoteListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stocks_list, container, false);
        ButterKnife.inject(this, view);
        swipeContainer.setOnRefreshListener(this);
        swipeContainer.setProgressBackgroundColor(R.color.gray);
        swipeContainer.setColorSchemeResources(R.color.red, R.color.lime);

        return view;
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View emptyFooter = getLayoutInflater(savedInstanceState).inflate(R.layout.empty_footer, null, false);
        getListView().addFooterView(emptyFooter);
        getListView().setFooterDividersEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
        App.getBus().register(this);
    }

    @Override public void onRefresh() {
        new UpdateDataTask().execute();
    }

    @Override public void onPause() {
        super.onPause();
        App.getBus().unregister(this);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public void refreshData() {
        QueryBuilder qb = App.getDaoSsn().getStockDao().queryBuilder();
        qb.where(new WhereCondition.StringCondition("SHOW_IN_QUOTES_LIST = 1 AND ISIN IN " +
                "(SELECT ISIN FROM CURRENT_QUOTE) ORDER BY NAME COLLATE LOCALIZED ASC"));
        List<Stock> items = qb.list();
        if (items == null || items.size() == 0) return;

        mAdapter = new QuoteListAdapter(App.get(), items);
        setListAdapter(mAdapter);
        if (lastUpdatedValueTV != null && dataFromValueTV != null) {
            lastUpdatedValueTV.setText(Utils.getFormattedDateAndTime(PrefsHelper.get().getLastUpdateTime()));
            dataFromValueTV.setText(Utils.getFormattedDateAndTime(getDataStamp(items)));
        }

        //add listener for long click (for deleting dialog)
        getListView().setOnItemLongClickListener(new ListView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                if (pos >= mAdapter.getCount()) return true;
                onListItemLongClick(pos, mAdapter.getItem(pos).getName());
                return true;
            }
        });
    }

    long getDataStamp(List<Stock> items) {
        long result = 0L;
        try {
            if (items != null && items.size() > 0) {
                for (Stock stock : items) {
                    if (stock != null
                            && stock.getCurrentQuote() != null
                            && stock.getCurrentQuote().getStamp() > result) {
                        result = stock.getCurrentQuote().getStamp();
                    }
                }
            }
        } catch (Exception e) {
            // Intentionally left blank
        }
        return result;
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        if (pos >= mAdapter.getCount()) return;
        Intent intent = new Intent(getActivity(), StockDetailActivity.class);
        intent.putExtra("isin", mAdapter.getItem(pos).getIsin());
        startActivity(intent);
    }

    public void onListItemLongClick(int pos, String quoteName) {
        if (pos >= mAdapter.getCount()) return;
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

    @OnClick(R.id.fabAdd) void onFabClicked(View v) {
        startActivity(new Intent(getActivity(), AddStockActivity.class));
    }

    @Subscribe public void onCurrentDataUpdated(CurrentDataUpdatedEvent event) {
        refreshData();
        finishSwipeToRefresh();
    }

    @Subscribe public void onInternetNotFound(InternetNotFoundEvent event) {
        finishSwipeToRefresh();
    }

    @Subscribe public void onUpdateError(UpdateErrorEvent event) {
        finishSwipeToRefresh();
    }

    void finishSwipeToRefresh() {
        if (swipeContainer != null) swipeContainer.setRefreshing(false);
    }
}