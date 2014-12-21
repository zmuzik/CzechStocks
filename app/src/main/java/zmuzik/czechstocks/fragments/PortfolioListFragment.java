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
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.activities.EditPortfolioItemActivity;
import zmuzik.czechstocks.adapters.PortfolioAdapter;
import zmuzik.czechstocks.dao.PortfolioItem;
import zmuzik.czechstocks.helpers.PrefsHelper;
import zmuzik.czechstocks.utils.Utils;

public class PortfolioListFragment extends ListFragment {

    final String TAG = this.getClass().getSimpleName();

    @InjectView(R.id.lastUpdatedValueTV) TextView lastUpdatedValueTV;
    @InjectView(R.id.dataFromValueTV) TextView dataFromValueTV;

    List<PortfolioItem> portfolioItems;
    PortfolioAdapter mAdapter;

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);
        ButterKnife.inject(this, view);
        return view;
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
            lastUpdatedValueTV.setText(Utils.getFormattedDateAndTime(PrefsHelper.get().getCurrentQuotesLut()));
            dataFromValueTV.setText(Utils.getFormattedDateAndTime(PrefsHelper.get().getCurrentQuotesTime()));
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

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        if (pos < mAdapter.getCount() - 1) {
            Intent intent = new Intent(getActivity(), EditPortfolioItemActivity.class);
            intent.putExtra("isin", mAdapter.getItem(pos).getIsin());
            startActivity(intent);
        }
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
}