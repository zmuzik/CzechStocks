package zmuzik.czechstocks.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


import zmuzik.czechstocks.App;
import zmuzik.czechstocks.fragments.PortfolioListFragment;
import zmuzik.czechstocks.fragments.QuoteListFragment;
import zmuzik.czechstocks.R;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private App app;

    QuoteListFragment stocksListFragment;
    PortfolioListFragment portfolioListFragment;

    public SectionsPagerAdapter(App app, FragmentManager fm) {
        super(fm);
        this.app = app;
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
                return app.getResources().getString(R.string.tab_title_section1);
            case 1:
                return app.getResources().getString(R.string.tab_title_section2);
        }
        return null;
    }
}