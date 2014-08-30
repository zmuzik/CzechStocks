package zmuzik.czechstocks;

import java.io.IOException;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class MyDaoGenerator {

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "zmuzik.czechstocks.dao");

        Entity currentTradingData = schema.addEntity("CurrentTradingData");
        currentTradingData.addStringProperty("isin").notNull().primaryKey();
        currentTradingData.addStringProperty("name").notNull();
        currentTradingData.addDoubleProperty("price").notNull();
        currentTradingData.addDoubleProperty("delta").notNull();
        currentTradingData.addDateProperty("stamp").notNull();

        Entity portfolioItem = schema.addEntity("PortfolioItem");
        portfolioItem.addStringProperty("isin").notNull().primaryKey();
        portfolioItem.addDoubleProperty("price").notNull();
        portfolioItem.addIntProperty("quantity").notNull();

        Entity stockListItem = schema.addEntity("StockListItem");
        stockListItem.addIdProperty();
        stockListItem.addStringProperty("isin").notNull();

        try {
            new DaoGenerator().generateAll(schema, args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
