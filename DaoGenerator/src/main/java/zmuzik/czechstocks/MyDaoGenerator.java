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
        currentQuote.addStringProperty("stamp").notNull();

        // Dividend
        Entity dividend = schema.addEntity("Dividend");
        dividend.addIdProperty();
        Property dividendIsinProperty = dividend.addStringProperty("isin").notNull().getProperty();
        dividend.addDoubleProperty("amount").notNull();
        dividend.addStringProperty("currency").notNull();
        dividend.addDateProperty("exDate");
        dividend.addDateProperty("paymentDate");

        // Stock Info
        Entity stockInfo = schema.addEntity("StockInfo");
        stockInfo.addIdProperty();
        Property stockInfoIsinProperty = stockInfo.addStringProperty("isin").notNull().getProperty();
        stockInfo.addStringProperty("indicator").notNull();
        stockInfo.addStringProperty("value").notNull();

        // Todays quote
        Entity todaysQuote = schema.addEntity("TodaysQuote");
        todaysQuote.addIdProperty();
        Property todaysQuoteIsinProperty = todaysQuote.addStringProperty("isin").notNull().getProperty();
        todaysQuote.addDateProperty("stamp").notNull();
        todaysQuote.addDoubleProperty("price").notNull();
        todaysQuote.addDoubleProperty("volume").notNull();

        // Stock
        Entity stock = schema.addEntity("Stock");
        Property stockIsinProperty = stock.addStringProperty("isin").notNull().primaryKey().getProperty();
        stock.addStringProperty("name").notNull();
        stock.addBooleanProperty("showInQuotesList").notNull();
        stock.addToOne(currentQuote, stockIsinProperty);
        stock.addToMany(todaysQuote, todaysQuoteIsinProperty);
        stock.addToMany(stockInfo, stockInfoIsinProperty);
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
