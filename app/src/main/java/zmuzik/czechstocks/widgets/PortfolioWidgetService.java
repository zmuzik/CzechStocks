package zmuzik.czechstocks.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.PortfolioItem;

public class PortfolioWidgetService extends RemoteViewsService {

    public final String TAG = this.getClass().getSimpleName();


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new PortfolioRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class PortfolioRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    public final String TAG = this.getClass().getSimpleName();
    private List<PortfolioItem> mWidgetItems = new ArrayList<PortfolioItem>();
    private Context mContext;
    private int mAppWidgetId;

    public PortfolioRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    // Initialize the data set.
    public void onCreate() {
        // In onCreate() you set up any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        mWidgetItems = App.getDaoSsn().getPortfolioItemDao().loadAll();
        Log.d(TAG, "created PortfolioRemoteViewsFactory");
        Log.d(TAG, "items: " + getCount());
    }

    @Override public void onDataSetChanged() {
    }

    @Override public void onDestroy() {
        mWidgetItems.clear();
    }

    @Override public int getCount() {
        return mWidgetItems == null ? 0 : mWidgetItems.size();
    }

    // Given the position (index) of a WidgetItem in the array, use the item's text value in
    // combination with the app widget item XML file to construct a RemoteViews object.
    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        // Construct a RemoteViews item based on the app widget item XML file, and set the
        // text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_portfolio);
        rv.setTextViewText(R.id.stockNameTV, mWidgetItems.get(position).getStock().getName());

        // Next, set a fill-intent, which will be used to fill in the pending intent template
        // that is set on the collection view in StackWidgetProvider.
//        Bundle extras = new Bundle();
//        extras.putInt(PortfolioWidgetProvider.EXTRA_ITEM, position);
//        Intent fillInIntent = new Intent();
//        fillInIntent.putExtras(extras);
//        // Make it possible to distinguish the individual on-click
//        // action of a given item
//        rv.setOnClickFillInIntent(R.id.list_item_portfolio, fillInIntent);

        // Return the RemoteViews object.
        return rv;
    }

    @Override public RemoteViews getLoadingView() {
        return null;
    }

    @Override public int getViewTypeCount() {
        return 1;
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public boolean hasStableIds() {
        return true;
    }
}
