package zmuzik.czechstocks;


import java.util.List;

import retrofit.http.GET;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.Dividend;
import zmuzik.czechstocks.dao.StockInfo;
import zmuzik.czechstocks.dao.TodaysQuote;

public interface ApiService {
    @GET("/current")
    List<CurrentQuote> getCurrentQuotes();

    @GET("/dividend")
    List<Dividend> getDividends();

    @GET("/stockinfo")
    List<StockInfo> getStockInfo();

    @GET("/todaysdata")
    List<TodaysQuote> getTodaysQuotes();
}
