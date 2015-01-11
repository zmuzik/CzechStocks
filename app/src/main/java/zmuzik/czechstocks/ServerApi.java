package zmuzik.czechstocks;


import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;
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

    @GET("/todaysQuotes/{stamp}")
    List<TodaysQuote> getTodaysQuotes(@Path("stamp") long stamp);

    @GET("/historicalQuotes/{stamp}")
    List<HistoricalQuote> getHistoricalQuotes(@Path("stamp") long stamp);
}
