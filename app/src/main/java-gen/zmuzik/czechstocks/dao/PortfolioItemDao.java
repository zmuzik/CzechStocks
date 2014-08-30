package zmuzik.czechstocks.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import zmuzik.czechstocks.dao.PortfolioItem;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table PORTFOLIO_ITEM.
*/
public class PortfolioItemDao extends AbstractDao<PortfolioItem, String> {

    public static final String TABLENAME = "PORTFOLIO_ITEM";

    /**
     * Properties of entity PortfolioItem.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Isin = new Property(0, String.class, "isin", true, "ISIN");
        public final static Property Price = new Property(1, double.class, "price", false, "PRICE");
        public final static Property Quantity = new Property(2, int.class, "quantity", false, "QUANTITY");
    };


    public PortfolioItemDao(DaoConfig config) {
        super(config);
    }
    
    public PortfolioItemDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'PORTFOLIO_ITEM' (" + //
                "'ISIN' TEXT PRIMARY KEY NOT NULL ," + // 0: isin
                "'PRICE' REAL NOT NULL ," + // 1: price
                "'QUANTITY' INTEGER NOT NULL );"); // 2: quantity
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'PORTFOLIO_ITEM'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, PortfolioItem entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getIsin());
        stmt.bindDouble(2, entity.getPrice());
        stmt.bindLong(3, entity.getQuantity());
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public PortfolioItem readEntity(Cursor cursor, int offset) {
        PortfolioItem entity = new PortfolioItem( //
            cursor.getString(offset + 0), // isin
            cursor.getDouble(offset + 1), // price
            cursor.getInt(offset + 2) // quantity
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, PortfolioItem entity, int offset) {
        entity.setIsin(cursor.getString(offset + 0));
        entity.setPrice(cursor.getDouble(offset + 1));
        entity.setQuantity(cursor.getInt(offset + 2));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(PortfolioItem entity, long rowId) {
        return entity.getIsin();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(PortfolioItem entity) {
        if(entity != null) {
            return entity.getIsin();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}