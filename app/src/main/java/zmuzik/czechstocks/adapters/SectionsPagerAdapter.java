package zmuzik.czechstocks.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;


import zmuzik.czechstocks.CzechStocksApp;
import zmuzik.czechstocks.PortfolioListFragment;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.StocksListFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private CzechStocksApp app;

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
                return app.getResources().getString(R.string.tab_title_section1);
            case 1:
                return app.getResources().getString(R.string.tab_title_section2);
        }
        return null;
    }
}