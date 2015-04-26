package zmuzik.czechstocks.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.events.InternetNotFoundEvent;
import zmuzik.czechstocks.fragments.PortfolioListFragment;
import zmuzik.czechstocks.fragments.QuoteListFragment;
import zmuzik.czechstocks.helpers.PrefsHelper;
import zmuzik.czechstocks.tasks.FillDbTablesTask;
import zmuzik.czechstocks.tasks.UpdateDataTask;
import zmuzik.czechstocks.ui.SlidingTabLayout;
import zmuzik.czechstocks.utils.DbUtils;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    private final String TAG = this.getClass().getSimpleName();

    @InjectView(R.id.viewPager) ViewPager viewPager;
    @InjectView(R.id.slidingTabs) SlidingTabLayout slidingTabs;

    private SectionsPagerAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        slidingTabs.setDistributeEvenly(true);
        slidingTabs.setViewPager(viewPager);

        //fill the default data if necessary
        if (!DbUtils.getInstance().isDataFilled()) new FillDbTablesTask(this).execute();
    }

    @Override protected void onResume() {
        super.onResume();
        App.getBus().register(this);
        if (PrefsHelper.get().isTimeToUpdateCurrent()) {
            new UpdateDataTask().execute();
        }
    }

    @Override public void onPause() {
        App.getBus().unregister(this);
        super.onPause();
    }

    @Override protected void onStop() {
        App.get().refreshAllWidgets();
        super.onStop();
    }

    @Override public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Subscribe public void onInternetNotFound(InternetNotFoundEvent event) {
        Toast.makeText(this, getString(R.string.internet_not_available), Toast.LENGTH_LONG).show();
    }

    class SectionsPagerAdapter extends FragmentPagerAdapter {

        QuoteListFragment stocksListFragment;
        PortfolioListFragment portfolioListFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (stocksListFragment == null) {
                        stocksListFragment = new QuoteListFragment();
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
                    return getResources().getString(R.string.quotes);
                case 1:
                    return getResources().getString(R.string.portfolio);
            }
            return null;
        }
    }
}
