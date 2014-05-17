package zmuzik.czechstocks;

import java.text.DecimalFormat;

import zmuzik.czechstocks.DaoMaster.DevOpenHelper;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class StocksListFragment extends ListFragment {

	final String TAG = this.getClass().getSimpleName();

	StocksCursorAdapter cursorAdapter;

	private SQLiteDatabase db;
	private Cursor cursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context context = this.getActivity().getApplicationContext();
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "czech-stocks-db", null);
		db = helper.getWritableDatabase();

		refreshData();
	}

	public void refreshData() {
		String select = "select t1._id as _id, t1.name as NAME, t1.delta as DELTA, t1.price as PRICE from stock t1, stock_list_item t2 where t1.isin = t2.isin order by t1.name collate localized asc;";
		cursor = db.rawQuery(select, null);
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
			super(context, layout, c, from, to);
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
				return decFormater.format(doubleAmount);
			}
			return text;
		}
	}
}