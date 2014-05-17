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

public class PortfolioListFragment extends ListFragment {

	final String TAG = this.getClass().getSimpleName();

	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private Cursor cursor;
	private StockDao stockDao;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this.getActivity().getApplicationContext(), "stocks-db",
				null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		stockDao = daoSession.getStockDao();

		String nameColumn = StockDao.Properties.Name.columnName;
		String orderBy = nameColumn + " COLLATE LOCALIZED ASC";
		String[] selColumns = { "_id", StockDao.Properties.Name.columnName, StockDao.Properties.Price.columnName,
				StockDao.Properties.Delta.columnName };
		cursor = db.query(stockDao.getTablename(), selColumns, null, null, null, null, orderBy);

		String[] from = { StockDao.Properties.Name.columnName, StockDao.Properties.Delta.columnName,
				StockDao.Properties.Price.columnName };
		int[] to = { R.id.stockNameTV, R.id.stockDeltaTV, R.id.stockPriceTV };

		StocksCursorAdapter adapter = new StocksCursorAdapter(this.getActivity(), R.layout.stocks_list_item, cursor,
				from, to);
		setListAdapter(adapter);

		//new UpdateDataTask().execute(stockDao);
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