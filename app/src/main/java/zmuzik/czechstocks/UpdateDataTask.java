package zmuzik.czechstocks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateDataTask extends AsyncTask {

    private final String TAG = this.getClass().getSimpleName();
    CzechStocksApp app;
    private boolean mDownloadError = false;

    private static final String STOCKS_URL = "http://185.8.238.141/czechstocks/api/stocks.json";
    private final int HTTP_OK = 200;
    private final int CONNECTION_TIMEOUT = 8000;
    private final int SOCKET_TIMEOUT = 8000;

    UpdateDataTask(Context context) {
        app = (CzechStocksApp) context;
    }

    @Override
    protected Object doInBackground(Object... params) {
        String response = downloadStockData(STOCKS_URL);
        if (!mDownloadError) {
            saveStocksToDb(response);
            app.setLastUpdatedTime();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (app != null && app.getMainActivity() != null) {
            app.getMainActivity().setStaticRefreshIcon();
            if (mDownloadError) {
                Toast.makeText(app, R.string.toas_check_net, Toast.LENGTH_LONG).show();
            } else {
                app.getMainActivity().refreshFragments();
            }
        }
    }

    public void saveStocksToDb(String serverResponse) {
        try {
            JSONArray stocksJsonArray = new JSONArray(serverResponse);
            app.getStockDao().deleteAll();
            for (int i = 0; i < stocksJsonArray.length(); i++) {
                JSONObject jsonObject = stocksJsonArray.getJSONObject(i);

                String isin = jsonObject.getString("isin");
                String name = jsonObject.getString("name");
                double price = jsonObject.getDouble("price");
                double delta = jsonObject.getDouble("delta");
                Date stamp = new SimpleDateFormat("d.M.yyyy HH:mm").parse(jsonObject.getString("stamp"));

                Stock stock = new Stock(null, isin, name, price, delta, stamp);
                app.getStockDao().insert(stock);

                //Log.i(TAG, jsonObject.toString());
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public String downloadStockDataOld(String url) {
        StringBuilder builder = new StringBuilder();

        HttpParams httpParameters = new BasicHttpParams();

        int timeoutConnection = CONNECTION_TIMEOUT;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);

        int timeoutSocket = SOCKET_TIMEOUT;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        HttpClient client = new DefaultHttpClient(httpParameters);
        HttpGet httpGet = new HttpGet(url);

        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == HTTP_OK) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(TAG, "Failed to download file");
            }
        } catch (Exception e) {
            mDownloadError = true;
            Crashlytics.logException(e);
        }
        return builder.toString();
    }

    public String downloadStockDataOld(String urlString) {
        InputStream input = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
                return null;
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            Log.i(TAG, "Starting downloading file from " + urlString);
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) {
                Log.e(TAG, "Error while closing io streams");
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
        Log.i(TAG, "Data saved to " + targetPath);
        return targetPath;
    }
}