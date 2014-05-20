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

public class PortfolioListFragment extends ListFragment {
	final String TAG = this.getClass().getSimpleName();
	CzechStocksApp app;
	PortfolioCursorAdapter cursorAdapter;
	private Cursor cursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (CzechStocksApp) this.getActivity().getApplicationContext();
		refreshData();
	}

	public void refreshData() {
		String select = "select _id, NAME, CURRENT_PRICE, DELTA, QUANTITY, ORIGINAL_PRICE, PROFIT from PORTFOLIO;";
		cursor = app.getDb().rawQuery(select, null);
		String[] from = { "NAME", "DELTA", "QUANTITY", "ORIGINAL_PRICE", "PROFIT" };

		int[] to = { R.id.portfolioStockNameTV, R.id.portfolioDeltaTV, R.id.portfolioQuantityTV,
				R.id.portfolioOriginalPriceTV, R.id.portfolioProfitTV };

		cursorAdapter = new PortfolioCursorAdapter(this.getActivity(), R.layout.portfolio_item, cursor, from, to);
		setListAdapter(cursorAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id) {
		getListView().setItemChecked(pos, true);
	}

	class PortfolioCursorAdapter extends SimpleCursorAdapter {

		private DecimalFormat decFormater = new DecimalFormat("#######0.00");

		public PortfolioCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to);
		}

		@Override
		public void setViewText(TextView v, String text) {
			super.setViewText(v, convText(v, text));
		}

		private String convText(TextView v, String text) {
			double doubleAmount;
			switch (v.getId()) {
			case R.id.portfolioOriginalPriceTV:
				doubleAmount = Double.valueOf(text);
				return " " + decFormater.format(doubleAmount) + " " + getResources().getString(R.string.currency);

			case R.id.portfolioProfitTV:
				doubleAmount = Double.valueOf(text);
				if (doubleAmount > 0) {
					v.setTextAppearance(app, R.style.greenNumber);
				} else if (doubleAmount < 0) {
					v.setTextAppearance(app, R.style.redNumber);
				}
				return decFormater.format(doubleAmount) + " " + getResources().getString(R.string.currency);

			case R.id.portfolioDeltaTV:
				doubleAmount = Double.valueOf(text);
				if (doubleAmount > 0) {
					v.setTextAppearance(app, R.style.greenNumber);
				} else if (doubleAmount < 0) {
					v.setTextAppearance(app, R.style.redNumber);
				}
				return decFormater.format(doubleAmount) + "%";

			case R.id.portfolioQuantityTV:
				int quantity = Integer.valueOf(text);
				return text + " " + getResources().getQuantityString(R.plurals.pieces_bought_at, quantity);

			}
			return text;
		}
	}
}