package zmuzik.czechstocks.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.adapters.PortfolioAdapter;
import zmuzik.czechstocks.dao.PortfolioItem;

public class PortfolioListFragment extends ListFragment {

    final String TAG = this.getClass().getSimpleName();

    @InjectView(R.id.lastUpdatedValue)
    TextView lastUpdatedValue;
    @InjectView(R.id.dataFromValue)
    TextView dataFromValue;

    List<PortfolioItem> portfolioItems;
    PortfolioAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshData();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.portfolio_fragment, container, false);
        ButterKnife.inject(getActivity());
        return view;
    }

    public void refreshData() {
        portfolioItems = App.get().getDaoSession().getPortfolioItemDao().loadAll();
        mAdapter = new PortfolioAdapter(App.get(), portfolioItems);
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {

    }
}