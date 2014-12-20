package zmuzik.czechstocks;

import java.io.IOException;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class MyDaoGenerator {

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "zmuzik.czechstocks.dao");

        // Current quote
        Entity currentQuote = schema.addEntity("CurrentQuote");
        currentQuote.addStringProperty("isin").notNull().primaryKey();
        currentQuote.addDoubleProperty("price").notNull();
        currentQuote.addDoubleProperty("delta").notNull();
        currentQuote.addStringProperty("timeStr").notNull();
        currentQuote.addLongProperty("stamp").notNull();

        // Dividend
        Entity dividend = schema.addEntity("Dividend");
        dividend.addIdProperty();
        Property dividendIsinProperty = dividend.addStringProperty("isin").notNull().getProperty();
        dividend.addDoubleProperty("amount").notNull();
        dividend.addStringProperty("currency").notNull();
        dividend.addLongProperty("exDate");
        dividend.addLongProperty("paymentDate");

        // Stock Detail
        Entity stockDetail = schema.addEntity("StockDetail");
        stockDetail.addIdProperty();
        Property stockDetailIsinProperty = stockDetail.addStringProperty("isin").notNull().getProperty();
        stockDetail.addStringProperty("indicator").notNull();
        stockDetail.addStringProperty("value").notNull();

        // Todays quote
        Entity todaysQuote = schema.addEntity("TodaysQuote");
        todaysQuote.addIdProperty();
        Property todaysQuoteIsinProperty = todaysQuote.addStringProperty("isin").notNull().getProperty();
        todaysQuote.addLongProperty("stamp").notNull();
        todaysQuote.addDoubleProperty("price").notNull();
        todaysQuote.addDoubleProperty("volume").notNull();

        // Historical quote
        Entity historicalQuote = schema.addEntity("HistoricalQuote");
        historicalQuote.addIdProperty();
        Property historicalQuoteIsinProperty = historicalQuote.addStringProperty("isin").notNull().getProperty();
        historicalQuote.addLongProperty("stamp").notNull();
        historicalQuote.addDoubleProperty("price").notNull();
        historicalQuote.addDoubleProperty("volume").notNull();

        // Stock
        Entity stock = schema.addEntity("Stock");
        Property stockIsinProperty = stock.addStringProperty("isin").notNull().primaryKey().getProperty();
        stock.addStringProperty("name").notNull();
        stock.addBooleanProperty("showInQuotesList").notNull();
        stock.addToOne(currentQuote, stockIsinProperty);
        stock.addToMany(todaysQuote, todaysQuoteIsinProperty);
        stock.addToMany(historicalQuote, historicalQuoteIsinProperty);
        stock.addToMany(stockDetail, stockDetailIsinProperty);
        stock.addToMany(dividend, dividendIsinProperty);

        // Portfolio item
        Entity portfolioItem = schema.addEntity("PortfolioItem");
        Property portfolioIsinProperty = portfolioItem.addStringProperty("isin").notNull().primaryKey().getProperty();
        portfolioItem.addDoubleProperty("price").notNull();
        portfolioItem.addIntProperty("quantity").notNull();
        portfolioItem.addToOne(stock, portfolioIsinProperty);

        try {
            new DaoGenerator().generateAll(schema, args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
