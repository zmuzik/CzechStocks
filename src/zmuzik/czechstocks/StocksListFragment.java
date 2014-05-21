package zmuzik.czechstocks;

import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class StocksListFragment extends ListFragment {

	final String TAG = this.getClass().getSimpleName();
	CzechStocksApp app;

	StocksCursorAdapter cursorAdapter;
	private Cursor cursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (CzechStocksApp) this.getActivity().getApplicationContext();
		refreshData();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.stocks_list_fragment, container, false);
		return view;
	}

	public void refreshData() {
		String select = "SELECT _id, NAME, DELTA, PRICE FROM STOCK_LIST;";
		cursor = app.getDb().rawQuery(select, null);
		String[] from = { "NAME", "DELTA", "PRICE" };
		int[] to = { R.id.stockNameTV, R.id.stockDeltaTV, R.id.stockPriceTV };

		cursorAdapter = new StocksCursorAdapter(this.getActivity(), R.layout.stocks_list_item, cursor, from, to);
		setListAdapter(cursorAdapter);
	}
	
	
	@Override
	public void onListItemClick(ListView l, View v, int pos, long id) {
		getListView().setItemChecked(pos, true);
	}

	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);

		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Resources res = getResources();
				final StockListItem sli = new StockListItem(arg3);
				final StockListItemDao sliDao = app.getStockListItemDao();
				String stockName =  ((TextView)((LinearLayout) arg1).getChildAt(0)).getText().toString();
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
						sliDao.delete(sli);
						app.getMainActivity().refreshFragments();
						dialog.dismiss();
					}
				});

				builder.show();
				return true;
			}
		});
	}

	class StocksCursorAdapter extends SimpleCursorAdapter {

		private DecimalFormat decFormater = new DecimalFormat("#######0.00");

		public StocksCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to, 0);
		}

		@Override
		public void setViewText(TextView v, String text) {
			super.setViewText(v, convText(v, text));
		}

		private String convText(TextView v, String text) {
			double doubleAmount;
			switch (v.getId()) {
			case R.id.stockPriceTV:
				doubleAmount = app.getDoubleValue(text);
				return decFormater.format(doubleAmount);
			case R.id.stockDeltaTV:
				doubleAmount = app.getDoubleValue(text);
				if (doubleAmount >= 0) {
					v.setTextAppearance(app, R.style.greenNumber);
				} else {
					v.setTextAppearance(app, R.style.redNumber);
				}
				return decFormater.format(doubleAmount) + "%";
			}
			return text;
		}
	}
}