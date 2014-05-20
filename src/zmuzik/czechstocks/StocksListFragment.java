package zmuzik.czechstocks;

import java.text.DecimalFormat;

import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
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
				doubleAmount = Double.valueOf(text);
				return decFormater.format(doubleAmount);
			case R.id.stockDeltaTV:
				doubleAmount = Double.valueOf(text);
				if (doubleAmount > 0) {
					v.setTextAppearance(app, R.style.greenNumber);
				} else if (doubleAmount < 0) {
					v.setTextAppearance(app, R.style.redNumber);
				}
				return decFormater.format(doubleAmount) + "%";
			}
			return text;
		}
	}
}