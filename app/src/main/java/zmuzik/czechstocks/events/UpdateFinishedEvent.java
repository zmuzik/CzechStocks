package zmuzik.czechstocks.events;

public class UpdateFinishedEvent {

    boolean historicalDataUpdated = false;
    boolean stockDataUpdated = false;

    public UpdateFinishedEvent(boolean histDataUpdated, boolean stockDataUpdated) {
        this.historicalDataUpdated = histDataUpdated;
        this.stockDataUpdated = stockDataUpdated;
    }

    public boolean isHistoricalDataUpdated() {
        return historicalDataUpdated;
    }

    public boolean isStockDataUpdated() {
        return stockDataUpdated;
    }
}