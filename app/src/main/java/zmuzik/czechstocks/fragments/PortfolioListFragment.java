package zmuzik.czechstocks.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.activities.AddPortfolioItemActivity;
import zmuzik.czechstocks.activities.EditPortfolioItemActivity;
import zmuzik.czechstocks.adapters.PortfolioAdapter;
import zmuzik.czechstocks.dao.PortfolioItem;
import zmuzik.czechstocks.events.UpdateFinishedEvent;
import zmuzik.czechstocks.helpers.PrefsHelper;
import zmuzik.czechstocks.tasks.UpdateDataTask;
import zmuzik.czechstocks.utils.Utils;

public class PortfolioListFragment extends ListFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    final String TAG = this.getClass().getSimpleName();

    @InjectView(R.id.lastUpdatedValueTV) TextView lastUpdatedValueTV;
    @InjectView(R.id.dataFromValueTV) TextView dataFromValueTV;
    @InjectView(R.id.updateTimeInfo) LinearLayout updateTimeInfo;
    @InjectView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;

    List<PortfolioItem> portfolioItems;
    PortfolioAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);
        ButterKnife.inject(this, view);
        swipeContainer.setOnRefreshListener(this);
        swipeContainer.setProgressBackgroundColor(R.color.gray);
        swipeContainer.setColorSchemeResources(R.color.red, R.color.lime);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
        App.getBus().register(this);
        if (PrefsHelper.get().isTimeToUpdateCurrent()) {
            new UpdateDataTask().execute();
        }
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
        portfolioItems = App.getDaoSsn().getPortfolioItemDao().loadAll();
        mAdapter = new PortfolioAdapter(App.get(), portfolioItems);
        setListAdapter(mAdapter);
        if (lastUpdatedValueTV != null && dataFromValueTV != null) {
            if (portfolioItems != null && portfolioItems.size() > 0) {
                updateTimeInfo.setVisibility(View.VISIBLE);
                lastUpdatedValueTV.setText(Utils.getFormattedDateAndTime(PrefsHelper.get().getLastUpdateTime()));
                dataFromValueTV.setText(Utils.getFormattedDateAndTime(getDataStamp(portfolioItems)));
            } else {
                updateTimeInfo.setVisibility(View.GONE);
            }
        }

        //add listener for long click (for deleting dialog)
        getListView().setOnItemLongClickListener(new ListView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                if (pos < mAdapter.getCount() - 1) {
                    onListItemLongClick(pos, mAdapter.getItem(pos).getStock().getName());
                }
                return true;
            }
        });
    }

    long getDataStamp(List<PortfolioItem> portfolioItems) {
        long result = 0L;
        try {
            if (portfolioItems != null && portfolioItems.size() > 0) {
                result = portfolioItems.get(0).getStock().getCurrentQuote().getStamp();
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return result;
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        if (pos == mAdapter.getCount() - 1) return;
        Intent intent = new Intent(getActivity(), EditPortfolioItemActivity.class);
        intent.putExtra("isin", mAdapter.getItem(pos).getIsin());
        startActivity(intent);
    }

    public void onListItemLongClick(int pos, String quoteName) {
        final int position = pos;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Resources res = getResources();
        builder.setTitle(res.getString(R.string.delete_from_portfolio_title));
        builder.setMessage(String.format(res.getString(R.string.do_you_want_to_delete_portfolio_item), quoteName));
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                PortfolioItem portfolioItem = mAdapter.getItem(position);
                App.getDaoSsn().getPortfolioItemDao().delete(portfolioItem);
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
        startActivity(new Intent(getActivity(), AddPortfolioItemActivity.class));
    }

    @Subscribe public void onUpdateFinished(UpdateFinishedEvent event) {
        refreshData();
        if (swipeContainer != null) swipeContainer.setRefreshing(false);
    }
}