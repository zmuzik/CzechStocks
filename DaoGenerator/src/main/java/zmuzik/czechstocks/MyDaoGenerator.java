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
        Entity currentTradingData = schema.addEntity("CurrentQuote");
        currentTradingData.addStringProperty("isin").notNull().primaryKey();
        currentTradingData.addStringProperty("name").notNull();
        currentTradingData.addDoubleProperty("price").notNull();
        currentTradingData.addDoubleProperty("delta").notNull();
        currentTradingData.addStringProperty("stamp").notNull();

        // Portfolio item
        Entity portfolioItem = schema.addEntity("PortfolioItem");
        Property portfolioIsinProperty = portfolioItem.addStringProperty("isin").notNull().primaryKey().getProperty();
        portfolioItem.addDoubleProperty("price").notNull();
        portfolioItem.addIntProperty("quantity").notNull();

        portfolioItem.addToOne(currentTradingData, portfolioIsinProperty);

        // Stock list item
        Entity quotationListItem = schema.addEntity("QuoteListItem");
        Property quotationListIsinProperty = quotationListItem.addStringProperty("isin").notNull().primaryKey().getProperty();

        quotationListItem.addToOne(currentTradingData, quotationListIsinProperty);

        try {
            new DaoGenerator().generateAll(schema, args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
