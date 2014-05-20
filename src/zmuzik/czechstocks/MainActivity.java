package zmuzik.czechstocks;

import java.util.ArrayList;
import java.util.List;

import zmuzik.czechstocks.R.layout;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity implements ActionBar.TabListener {

	private final String TAG = this.getClass().getSimpleName();
	private CzechStocksApp app;

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private MenuItem mRefreshMenuItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (CzechStocksApp) getApplicationContext();
		app.setMainActiviy(this);

		setContentView(R.layout.activity_main);
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
		}
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		mRefreshMenuItem = menu.findItem(R.id.action_refresh);
		actionDataRefresh();
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_edit:
			if (mViewPager.getCurrentItem() == 0) {
				actionEditStockList();
			} else {
				actionAddPortfolioItem();
			}
			break;
		case R.id.action_refresh:
			mRefreshMenuItem = item;
			actionDataRefresh();
			break;
		default:
			break;
		}

		return true;
	}

	void actionEditStockList() {
		List<Stock> allStocks = app.getStockDao().loadAll();
		List<StockListItem> allStockListItems = app.getStockListItemDao().loadAll();
		ArrayList<String> allStockListItemsStrings = new ArrayList<String>();
		for (StockListItem item : allStockListItems) {
			allStockListItemsStrings.add(item.getIsin());
		}

		String[] stockNames = new String[allStocks.size()];
		final String[] stockIsins = new String[allStocks.size()];
		final boolean[] selectedStocks = new boolean[allStocks.size()];

		int i = 0;
		for (Stock stock : allStocks) {
			stockNames[i] = stock.getName();
			stockIsins[i] = stock.getIsin();
			selectedStocks[i] = allStockListItemsStrings.contains(stock.getIsin());
			i++;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.add_stocks_dialog_title);

		builder.setMultiChoiceItems(stockNames, selectedStocks, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				selectedStocks[which] = isChecked;
			}
		});

		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				app.getStockListItemDao().deleteAll();
				for (int i = 0; i < selectedStocks.length; i++) {
					if (selectedStocks[i]) {
						app.getStockListItemDao().insert(new StockListItem((long) i, stockIsins[i]));
					}
				}
				refreshFragments();
			}
		});

		builder.show();
	}

	void actionAddPortfolioItem() {
		final List<Stock> allStocks = app.getStockDao().loadAll();
		ArrayList<String> stockNames = new ArrayList<String>();
		for (Stock item : allStocks) {
			stockNames.add(item.getName());
		}

		LayoutInflater inflater = this.getLayoutInflater();
		LinearLayout parentLayout = (LinearLayout) inflater.inflate(R.layout.add_portfolio_item_dialog, null);

		final Spinner spinner = (Spinner) parentLayout.getChildAt(0);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(app, android.R.layout.simple_spinner_dropdown_item,
				stockNames);
		spinner.setAdapter(adapter);

		final EditText quantityET = (EditText) parentLayout.getChildAt(1);
		final EditText priceET = (EditText) parentLayout.getChildAt(2);

		// init builder
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.add_portfolio_item_dialog_title);
		builder.setCancelable(true);

		// set and inflate layout
		builder.setView(parentLayout);

		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PortfolioItemDao pid = app.getPortfolioItemDao();
				PortfolioItem pi = new PortfolioItem();

				int quantity = Integer.valueOf(quantityET.getText().toString());
				double price = Double.valueOf(priceET.getText().toString());
				if (quantity > 0 && price > 0) {
					int position = spinner.getSelectedItemPosition();
					Stock stock = allStocks.get(position);

					pi.setIsin(stock.getIsin());
					pi.setPrice(price);
					pi.setQuantity(quantity);
					pid.insert(pi);
					refreshFragments();
				}
				dialog.dismiss();
			}
		});

		builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();

	}

	void actionDataRefresh() {
		setMovingRefreshIcon();
		new UpdateDataTask(app).execute();
	}

	void setMovingRefreshIcon() {
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ImageView iv = (ImageView) inflater.inflate(layout.refresh_action_view, null);
		Animation rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
		rotation.setRepeatCount(Animation.INFINITE);
		iv.startAnimation(rotation);
		mRefreshMenuItem.setActionView(iv);
	}

	void setStaticRefreshIcon() {
		if (mRefreshMenuItem != null && mRefreshMenuItem.getActionView() != null) {
			mRefreshMenuItem.getActionView().clearAnimation();
			mRefreshMenuItem.setActionView(null);
		}
	}

	void refreshFragments() {
		if (mSectionsPagerAdapter != null) {
			if (mSectionsPagerAdapter.stocksListFragment != null) {
				mSectionsPagerAdapter.stocksListFragment.refreshData();
			}
			if (mSectionsPagerAdapter.portfolioListFragment != null) {
				mSectionsPagerAdapter.portfolioListFragment.refreshData();
			}
		}
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		;
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		StocksListFragment stocksListFragment;
		PortfolioListFragment portfolioListFragment;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				if (stocksListFragment == null) {
					stocksListFragment = new StocksListFragment();
				}
				return stocksListFragment;
			case 1:
				if (portfolioListFragment == null) {
					portfolioListFragment = new PortfolioListFragment();
				}
				return portfolioListFragment;
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.tab_title_section1);
			case 1:
				return getString(R.string.tab_title_section2);
			}
			return null;
		}
	}

}
