package zmuzik.czechstocks.events;

public class UpdateFinishedEvent {

    boolean historicalDataUpdated = false;

    public UpdateFinishedEvent(boolean historicalDataUpdated) {
        this.historicalDataUpdated = historicalDataUpdated;
    }

    public boolean isHistoricalDataUpdated() {
        return historicalDataUpdated;
    }
}