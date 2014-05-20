package zmuzik.czechstocks;

import java.text.DecimalFormat;
import java.util.StringTokenizer;

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
import android.widget.EditText;
import android.widget.LinearLayout;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.portfolio_fragment, container, false);
		return view;
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

	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);

		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Resources res = getResources();
				final long rowid = arg3;

				LinearLayout itemRow1 = ((LinearLayout) ((LinearLayout) arg1).getChildAt(0));
				LinearLayout itemRow2 = ((LinearLayout) ((LinearLayout) arg1).getChildAt(1));
				String stockName = ((TextView) itemRow1.getChildAt(0)).getText().toString();

				String quantityString = ((TextView) itemRow2.getChildAt(0)).getText().toString();
				StringTokenizer st = new StringTokenizer(quantityString, " ");
				int quantity = Integer.valueOf(st.nextToken());

				String origPriceString = ((TextView) itemRow2.getChildAt(1)).getText().toString();
				st = new StringTokenizer(origPriceString, " ");
				double origPrice = Double.valueOf(st.nextToken());

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
								PortfolioItemDao pid = app.getPortfolioItemDao();
								pid.deleteByKey(rowid);
								refreshData();
								dialog.dismiss();
							}
						});

				dialogBuilder.setPositiveButton(res.getString(R.string.button_save),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								double price = Double.valueOf(priceET.getText().toString());
								int quantity = Integer.valueOf(quantityET.getText().toString());
								if (price > 0 && quantity > 0) {
									PortfolioItemDao pid = app.getPortfolioItemDao();
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

	class PortfolioCursorAdapter extends SimpleCursorAdapter {

		private DecimalFormat decFormater = new DecimalFormat("#######0.00");

		public PortfolioCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to, 0);
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
				if (doubleAmount >= 0) {
					v.setTextAppearance(app, R.style.greenNumber);
				} else {
					v.setTextAppearance(app, R.style.redNumber);
				}
				return decFormater.format(doubleAmount) + " " + getResources().getString(R.string.currency);

			case R.id.portfolioDeltaTV:
				doubleAmount = Double.valueOf(text);
				if (doubleAmount >= 0) {
					v.setTextAppearance(app, R.style.greenNumber);
				} else {
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