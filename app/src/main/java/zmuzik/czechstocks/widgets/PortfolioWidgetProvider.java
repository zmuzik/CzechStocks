package zmuzik.czechstocks.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.List;
import java.util.Random;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.tasks.UpdatePortfolioWidgetTask;
import zmuzik.czechstocks.utils.Utils;

public class PortfolioWidgetProvider extends AppWidgetProvider {

    public static final String TAG = "PortfolioWidgetProvider";
    public static final String ACTION_PORTFOLIO_WIDGET_REFRESH = "zmuzik.czechstocks.ACTION_PORTFOLIO_WIDGET_REFRESH";
    public static int[] mAppWidgetIds;

    private Random randomGenerator = new Random();
    public static int randomNumber;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        mAppWidgetIds = appWidgetIds;
        new UpdatePortfolioWidgetTask(context).execute();
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        randomNumber = randomGenerator.nextInt(Integer.MAX_VALUE - 1);
        String action = intent.getAction();
        if (ACTION_PORTFOLIO_WIDGET_REFRESH.equals(action)) {
            for (int i = 0; i < mAppWidgetIds.length; i++) {
                refreshAppWidget(context, AppWidgetManager.getInstance(context), mAppWidgetIds[i]);
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void refreshAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(TAG, "updating widget id: " + appWidgetId);
        Intent intent = new Intent(context, PortfolioWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.fromParts("content",
                String.valueOf(appWidgetId + randomNumber), null));
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_portfolio);
        rv.setRemoteAdapter(R.id.listView, intent);
        rv.setEmptyView(R.id.listView, R.id.emptyView);
        rv.setTextViewText(R.id.dataFromValueTV, Utils.getFormattedDateAndTime(getDataTimestamp()));

        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    static long getDataTimestamp() {
        List<CurrentQuote> allQuotes = App.getDaoSsn().getCurrentQuoteDao().loadAll();
        if (allQuotes == null || allQuotes.size() == 0) return 0L;
        CurrentQuote quote = allQuotes.get(0);
        return quote.getStamp();
    }
}


