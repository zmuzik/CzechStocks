package zmuzik.czechstocks;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import com.crashlytics.android.Crashlytics;

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
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class PortfolioListFragment extends ListFragment {
	
	final String TAG = this.getClass().getSimpleName();
	CzechStocksApp app;
	TextView mLastUpdateInfoTV;
	PortfolioCursorAdapter cursorAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (CzechStocksApp) this.getActivity().getApplicationContext();
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
		mLastUpdateInfoTV = (TextView)((RelativeLayout)view).getChildAt(2);
		return view;
	}

	public void refreshData() {
		String select = "select _id, NAME, CURRENT_PRICE, DELTA, QUANTITY, ORIGINAL_PRICE, PROFIT from TOTAL_PORTFOLIO;";
		if (app.isTableEmpty(app.getDb(), "PORTFOLIO")) {
			select = "select _id, NAME, CURRENT_PRICE, DELTA, QUANTITY, ORIGINAL_PRICE, PROFIT from PORTFOLIO;";
		}
		
		Cursor cursor = app.getDb().rawQuery(select, null);
		String[] from = { "NAME", "DELTA", "QUANTITY", "ORIGINAL_PRICE", "PROFIT" };

		int[] to = { R.id.portfolioStockNameTV, R.id.portfolioDeltaTV, R.id.portfolioQuantityTV,
				R.id.portfolioOriginalPriceTV, R.id.portfolioProfitTV };

		if (cursor != null) {
			Crashlytics.setInt("portfolioSize", cursor.getCount());
			Crashlytics.setBool("portfolioCursorNull", false);
		} else {
			Crashlytics.setBool("portfolioCursorNull", true);			
		}
		
		cursorAdapter = new PortfolioCursorAdapter(app, R.layout.portfolio_item, cursor, from, to);
		setListAdapter(cursorAdapter);
		
		if (mLastUpdateInfoTV != null)  {
			mLastUpdateInfoTV.setText(app.getlastUpdateInfoString());
		}
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
				
				if ("".equals(quantityString)) {
					return false;
				}
				
				StringTokenizer st = new StringTokenizer(quantityString, " ");
				int quantity = Integer.valueOf(st.nextToken());

				String origPriceString = ((TextView) itemRow2.getChildAt(1)).getText().toString();
				st = new StringTokenizer(origPriceString, " ");

				double origPrice = app.getDoubleValue(st.nextToken());

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
								double price = app.getDoubleValue(priceET.getText().toString());
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

		private DecimalFormat decFormater;

		public PortfolioCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to, 0);
			decFormater = new DecimalFormat();
			decFormater.setMinimumIntegerDigits(1);
			decFormater.setMinimumFractionDigits(2);
			decFormater.setMaximumFractionDigits(2);
		}

		@Override
		public void setViewText(TextView v, String text) {
			super.setViewText(v, convText(v, text));
		}

		private String convText(TextView v, String text) {
			double doubleAmount;
			switch (v.getId()) {
			case R.id.portfolioOriginalPriceTV:
				doubleAmount = app.getDoubleValue(text);
				if (doubleAmount == 0) {
					return "";
				} else {
					return " " + decFormater.format(doubleAmount) + " " + getResources().getString(R.string.currency);					
				}

			case R.id.portfolioProfitTV:
				doubleAmount = app.getDoubleValue(text);
				if (doubleAmount >= 0) {
					v.setTextAppearance(app, R.style.greenNumber);
				} else {
					v.setTextAppearance(app, R.style.redNumber);
				}
				return decFormater.format(doubleAmount) + " " + getResources().getString(R.string.currency);

			case R.id.portfolioDeltaTV:
				doubleAmount = app.getDoubleValue(text);
				if (doubleAmount >= 0) {
					v.setTextAppearance(app, R.style.greenNumber);
				} else {
					v.setTextAppearance(app, R.style.redNumber);
				}
				return decFormater.format(doubleAmount) + "%";

			case R.id.portfolioQuantityTV:
				int quantity = Integer.valueOf(text);
				if (quantity == 0) {
					return "";
				} else {
					return text + " " + getResources().getQuantityString(R.plurals.pieces_bought_at, quantity);					
				}
			case R.id.portfolioStockNameTV:
				if ("TOTAL".equals(text)) {
					Locale locale = getResources().getConfiguration().locale;
					if (locale.getLanguage().equals("cs")) {
						return "CELKEM";
					} else {
						return text;
					}
				}
			}
			return text;
		}
	}
}