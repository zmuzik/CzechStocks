package zmuzik.czechstocks.widgets;

import android.content.Intent;
import android.widget.RemoteViewsService;

import zmuzik.czechstocks.adapters.PortfolioAdapter;

public class PortfolioWidgetService extends RemoteViewsService {

    public final String TAG = this.getClass().getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new PortfolioAdapter(this.getApplicationContext(), intent);
    }
}


