package zmuzik.czechstocks.tasks;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.lang.ref.WeakReference;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.events.CurrentDataUpdatedEvent;
import zmuzik.czechstocks.helpers.PrefsHelper;
import zmuzik.czechstocks.utils.TimeUtils;
import zmuzik.czechstocks.widgets.PortfolioWidgetProvider;

public class UpdatePortfolioWidgetTask extends UpdateDataTask {

    private final String TAG = this.getClass().getSimpleName();
    private WeakReference<Context> mContext;

    public UpdatePortfolioWidgetTask(Context context) {
        mContext = new WeakReference<Context>(context);
    }

    @Override
    protected Object doInBackground(Object... params) {
        try {
            if (updateCurrentQuotes()) {
                PrefsHelper.get().setLastUpdateTime(TimeUtils.getNow());
                App.getDaoSsn().clear();
                App.getBus().post(new CurrentDataUpdatedEvent());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Crashlytics.logException(e);
        }
        return null;
    }

    @Override protected void onPostExecute(Object o) {
        Context ctx = mContext.get();
        if (ctx == null) return;
        Intent intent = new Intent(ctx, PortfolioWidgetProvider.class);
        intent.setAction(PortfolioWidgetProvider.ACTION_PORTFOLIO_WIDGET_REFRESH);
        ctx.sendBroadcast(intent);
        super.onPostExecute(o);
    }
}