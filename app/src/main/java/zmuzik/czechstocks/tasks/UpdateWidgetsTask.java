package zmuzik.czechstocks.tasks;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import zmuzik.czechstocks.App;
import zmuzik.czechstocks.events.CurrentDataUpdatedEvent;
import zmuzik.czechstocks.helpers.PrefsHelper;
import zmuzik.czechstocks.utils.TimeUtils;

public class UpdateWidgetsTask extends UpdateDataTask {

    private final String TAG = this.getClass().getSimpleName();

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
        }
        return null;
    }

    @Override protected void onPostExecute(Object o) {
        App.get().refreshAllWidgets();
        super.onPostExecute(o);
    }
}