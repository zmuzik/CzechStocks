package zmuzik.czechstocks;


import java.util.List;

import retrofit.http.GET;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.Dividend;
import zmuzik.czechstocks.dao.HistoricalQuote;
import zmuzik.czechstocks.dao.StockInfo;
import zmuzik.czechstocks.dao.TodaysQuote;

public interface ServerApi {

    @GET("/currentQuote")
    List<CurrentQuote> getCurrentQuotes();

    @GET("/dividend")
    List<Dividend> getDividends();

    @GET("/stockInfo")
    List<StockInfo> getStockInfo();

    @GET("/todaysQuote")
    List<TodaysQuote> getTodaysQuotes();

    @GET("/historicalQuote")
    List<HistoricalQuote> getHistoricalQuotes();
}
