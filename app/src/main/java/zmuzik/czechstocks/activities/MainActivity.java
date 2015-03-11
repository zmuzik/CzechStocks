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

import butterknife.ButterKnife;
import butterknife.InjectView;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.fragments.PortfolioListFragment;
import zmuzik.czechstocks.fragments.QuoteListFragment;
import zmuzik.czechstocks.tasks.FillDbTablesTask;
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
    }

    @Override public void onPause() {
        super.onPause();
    }

    @Override public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {
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
