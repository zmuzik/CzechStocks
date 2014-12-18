package zmuzik.czechstocks;


import java.util.List;

import retrofit.http.GET;
import zmuzik.czechstocks.dao.CurrentQuote;
import zmuzik.czechstocks.dao.Dividend;
import zmuzik.czechstocks.dao.HistoricalQuote;
import zmuzik.czechstocks.dao.StockDetail;
import zmuzik.czechstocks.dao.TodaysQuote;

public interface ServerApi {

    @GET("/currentQuotes")
    List<CurrentQuote> getCurrentQuotes();

    @GET("/dividends")
    List<Dividend> getDividends();

    @GET("/stockDetails")
    List<StockDetail> getStockDetails();

    @GET("/todaysQuotes")
    List<TodaysQuote> getTodaysQuotes();

    @GET("/historicalQuotes")
    List<HistoricalQuote> getHistoricalQuotes();
}
