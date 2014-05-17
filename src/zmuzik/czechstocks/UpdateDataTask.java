package zmuzik.czechstocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class UpdateDataTask extends AsyncTask {

	private final String TAG = this.getClass().getSimpleName();
	CzechStocksApp app;
	
	private static final String STOCKS_URL = "http://162.252.242.87:3001/stocks.json";
	private final int HTTP_OK = 200;

	UpdateDataTask(Context context) {
		app = (CzechStocksApp) context;
	}

	@Override
	protected Object doInBackground(Object... params) {
		String response = downloadStockData(STOCKS_URL);
		saveStocksToDb(response);
		return null;
	}

	@Override
	protected void onPostExecute(Object result) {
		if (app != null && app.getMainActivity() != null) {
			app.getMainActivity().refreshFragments();
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
				Date stamp = new SimpleDateFormat("M.d.yyyy HH:mm").parse(jsonObject.getString("stamp"));

				Stock stock = new Stock(null, isin, name, price, delta, stamp);
				app.getStockDao().insert(stock);

				Log.i(TAG, jsonObject.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String downloadStockData(String url) {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
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
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}
}