package zmuzik.czechstocks.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
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
        Log.d(TAG, "onUpdate called");
        mAppWidgetIds = appWidgetIds;
        new UpdatePortfolioWidgetTask().execute();
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive - action: " + action);
        ComponentName name = new ComponentName(context, PortfolioWidgetProvider.class);
        mAppWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
        if (mAppWidgetIds == null) return;
        randomNumber = randomGenerator.nextInt(Integer.MAX_VALUE - 1);
        if (ACTION_PORTFOLIO_WIDGET_REFRESH.equals(action)) {
            for (int id : mAppWidgetIds) {
                refreshAppWidget(context, AppWidgetManager.getInstance(context), id);
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
        Log.d(TAG, "refreshing widget id: " + appWidgetId);
        Intent intent = new Intent(context, PortfolioWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        Uri uri = Uri.fromParts("content", String.valueOf(appWidgetId + randomNumber), null);
        intent.setData(uri);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_portfolio);
        remoteViews.setRemoteAdapter(R.id.listView, intent);
        remoteViews.setEmptyView(R.id.listView, R.id.emptyView);
        remoteViews.setTextViewText(R.id.dataFromValueTV, Utils.getFormattedDateAndTime(getDataTimestamp()));

        Intent refreshIntent = new Intent(context, PortfolioWidgetProvider.class);
        refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//        refreshIntent.setData(uri);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, mAppWidgetIds);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.root, pendingIntent);
        remoteViews.setPendingIntentTemplate(R.id.listView, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    static long getDataTimestamp() {
        List<CurrentQuote> allQuotes = App.getDaoSsn().getCurrentQuoteDao().loadAll();
        if (allQuotes == null || allQuotes.size() == 0) return 0L;
        CurrentQuote quote = allQuotes.get(0);
        return quote.getStamp();
    }
}


