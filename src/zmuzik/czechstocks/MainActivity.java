package zmuzik.czechstocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import zmuzik.czechstocks.DaoMaster.DevOpenHelper;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity implements ActionBar.TabListener {
	final String TAG = this.getClass().getSimpleName();
	final String DB_NAME = "czech-stocks-db";

	SQLiteDatabase db;
	DaoMaster daoMaster;
	DaoSession daoSession;
	StockDao stockDao;
	StockListItemDao stockListItemDao;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initDb();

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
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
		}
	}

	void initDb() {
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this.getApplicationContext(), "czech-stocks-db", null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		stockDao = daoSession.getStockDao();
		stockListItemDao = daoSession.getStockListItemDao();

		StockDao.createTable(db, true);
		StockListItemDao.createTable(db, true);
		PortfolioItemDao.createTable(db, true);

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
			actionEditStockList();
			break;
		case R.id.action_refresh:
			actionRefresh();
			break;
		case R.id.action_settings:
			actionSettings();
			break;
		default:
			break;
		}

		return true;
	}

	void actionEditStockList() {
		List<Stock> allStocks = stockDao.loadAll();
		List<StockListItem> allStockListItems = stockListItemDao.loadAll();
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

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				stockListItemDao.deleteAll();
				for (int i = 0; i < selectedStocks.length; i++) {
					if (selectedStocks[i]) {
						stockListItemDao.insert(new StockListItem((long) i, stockIsins[i]));
					}
				}
				refreshFragments();
			}
		});

		builder.show();
	}

	void actionRefresh() {
		Toast.makeText(this, "Add or remove selected", Toast.LENGTH_SHORT).show();
	}

	void actionSettings() {
		Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
	}

	void refreshFragments() {
		StocksListFragment fragment = (StocksListFragment) mSectionsPagerAdapter.getItem(0);
		fragment.refreshData();
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
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
			Locale l = Locale.getDefault();
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
