package zmuzik.czechstocks.dao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table CURRENT_QUOTE.
 */
public class CurrentQuote {

    /** Not-null value. */
    private String isin;
    private double price;
    private double delta;
    /** Not-null value. */
    private String timeStr;

    public CurrentQuote() {
    }

    public CurrentQuote(String isin) {
        this.isin = isin;
    }

    public CurrentQuote(String isin, double price, double delta, String timeStr) {
        this.isin = isin;
        this.price = price;
        this.delta = delta;
        this.timeStr = timeStr;
    }

    /** Not-null value. */
    public String getIsin() {
        return isin;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setIsin(String isin) {
        this.isin = isin;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    /** Not-null value. */
    public String getTimeStr() {
        return timeStr;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

}
