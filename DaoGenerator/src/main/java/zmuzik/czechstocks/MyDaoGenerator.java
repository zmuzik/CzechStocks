package zmuzik.czechstocks;

import java.io.IOException;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class MyDaoGenerator {

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "zmuzik.czechstocks.dao");

        // Current trading data
        Entity currentQuote = schema.addEntity("CurrentQuote");
        currentQuote.addStringProperty("isin").notNull().primaryKey();
        currentQuote.addDoubleProperty("price").notNull();
        currentQuote.addDoubleProperty("delta").notNull();
        currentQuote.addStringProperty("stamp").notNull();

        // Stock list item
        Entity stock = schema.addEntity("Stock");
        Property quotationListIsinProperty = stock.addStringProperty("isin").notNull().primaryKey().getProperty();
        stock.addStringProperty("name").notNull();
        stock.addBooleanProperty("showInQuotesList").notNull();
        stock.addToOne(currentQuote, quotationListIsinProperty);

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
