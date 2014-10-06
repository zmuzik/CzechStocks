package zmuzik.czechstocks;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import zmuzik.czechstocks.adapters.SectionsPagerAdapter;
import zmuzik.czechstocks.dao.PortfolioItem;
import zmuzik.czechstocks.dao.PortfolioItemDao;
import zmuzik.czechstocks.dao.Stock;

public class MainActivity extends Activity implements ActionBar.TabListener {

    private final String TAG = this.getClass().getSimpleName();
    private App app;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private MenuItem mRefreshMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (App) getApplicationContext();
        app.setMainActiviy(this);

        setContentView(R.layout.activity_main);
        final ActionBar actionBar = getActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mSectionsPagerAdapter = new SectionsPagerAdapter(app, getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

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
        Intent intent = new Intent(this, EditStockListActivity.class);
        startActivity(intent);
    }

    void actionAddPortfolioItem() {
        final List<Stock> allStocks = app.getDaoSession().getStockDao().loadAll();
        ArrayList<String> stockNames = new ArrayList<String>();
        for (Stock stock : allStocks) {
            stockNames.add(stock.getName());
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
                String quantityString = quantityET.getText().toString();
                String priceString = priceET.getText().toString();
                Resources res = getResources();
                if (quantityString == null || "".equals(quantityString)) {
                    Toast.makeText(app, res.getString(R.string.number_of_stocks_null), Toast.LENGTH_LONG).show();
                    return;
                }
                if (priceString == null || "".equals(priceString)) {
                    Toast.makeText(app, res.getString(R.string.average_price_null), Toast.LENGTH_LONG).show();
                    return;
                }
                PortfolioItemDao pid = app.getDaoSession().getPortfolioItemDao();
                PortfolioItem pi = new PortfolioItem();
                int quantity = Integer.valueOf(quantityString);
                double price = app.getDoubleValue(priceET.getText().toString());
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
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);
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
            if (mSectionsPagerAdapter.getItem(0) != null) {
                ((QuoteListFragment) mSectionsPagerAdapter.getItem(0)).refreshData();
            }
            if (mSectionsPagerAdapter.getItem(1) != null) {
                ((PortfolioListFragment) mSectionsPagerAdapter.getItem(1)).refreshData();
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

}
