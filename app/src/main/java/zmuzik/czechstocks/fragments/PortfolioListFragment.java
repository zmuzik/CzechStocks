package zmuzik.czechstocks.fragments;

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.StringTokenizer;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.Utils;
import zmuzik.czechstocks.adapters.PortfolioAdapter;
import zmuzik.czechstocks.dao.PortfolioItem;
import zmuzik.czechstocks.dao.PortfolioItemDao;

public class PortfolioListFragment extends ListFragment {

    final String TAG = this.getClass().getSimpleName();
    TextView mLastUpdateTime;
    TextView mDataFromTime;
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
        mLastUpdateTime = (TextView) view.findViewById(R.id.lastUpdatedValue);
        mDataFromTime = (TextView) view.findViewById(R.id.dataFromValue);
        return view;
    }

    public void refreshData() {
        List items = App.get().getDaoSession().getPortfolioItemDao().loadAll();
        mAdapter = new PortfolioAdapter(App.get(), items);
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Resources res = getResources();
                final long rowid = arg3;

                PortfolioItemDao pid = App.get().getDaoSession().getPortfolioItemDao();
                List<PortfolioItem> portfolioItems = pid.loadAll();
                PortfolioItem clickedItem = portfolioItems.get(arg2);
                final String isin = clickedItem.getIsin();

                LinearLayout itemRow1 = ((LinearLayout) ((LinearLayout) arg1).getChildAt(0));
                LinearLayout itemRow2 = ((LinearLayout) ((LinearLayout) arg1).getChildAt(1));
                String stockName = ((TextView) itemRow1.getChildAt(0)).getText().toString();

                String quantityString = ((TextView) itemRow2.getChildAt(0)).getText().toString();

                if ("".equals(quantityString)) {
                    return false;
                }

                StringTokenizer st = new StringTokenizer(quantityString, " ");
                int quantity = Integer.valueOf(st.nextToken());

                String origPriceString = ((TextView) itemRow2.getChildAt(1)).getText().toString();
                st = new StringTokenizer(origPriceString, " ");

                double origPrice = Utils.getDoubleValue(st.nextToken());

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();

                LinearLayout parentLayout = (LinearLayout) inflater.inflate(R.layout.edit_portfolio_item_dialog, null);

                TextView titleTV = (TextView) parentLayout.getChildAt(0);
                final EditText quantityET = (EditText) ((LinearLayout) parentLayout.getChildAt(1)).getChildAt(1);
                final EditText priceET = (EditText) ((LinearLayout) parentLayout.getChildAt(2)).getChildAt(1);

                quantityET.setText(String.valueOf(quantity));
                priceET.setText(String.valueOf(origPrice));
                titleTV.setText(stockName);

                dialogBuilder.setTitle(R.string.edit_remove_title);
                dialogBuilder.setView(parentLayout);

                dialogBuilder.setCancelable(true);

                dialogBuilder.setNegativeButton(res.getString(R.string.button_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                dialogBuilder.setNeutralButton(res.getString(R.string.button_remove),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                PortfolioItemDao pid = App.get().getDaoSession().getPortfolioItemDao();
                                pid.deleteByKey(isin);
                                refreshData();
                                dialog.dismiss();
                            }
                        });

                dialogBuilder.setPositiveButton(res.getString(R.string.button_save),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                double price = Utils.getDoubleValue(priceET.getText().toString());
                                int quantity = Integer.valueOf(quantityET.getText().toString());
                                if (price > 0 && quantity > 0) {
                                    PortfolioItemDao pid = App.get().getDaoSession().getPortfolioItemDao();
                                    PortfolioItem pi = pid.loadByRowId(rowid);
                                    pi.setPrice(price);
                                    pi.setQuantity(quantity);
                                    pid.update(pi);
                                    refreshData();
                                }

                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
                return true;
            }
        });
    }
}