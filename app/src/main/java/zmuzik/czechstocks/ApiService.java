package zmuzik.czechstocks;


import java.util.List;

import retrofit.http.GET;
import zmuzik.czechstocks.dao.CurrentQuote;

public interface ApiService {
    @GET("/stocks.json")
    List<CurrentQuote> getCurrentQuotes();
}
