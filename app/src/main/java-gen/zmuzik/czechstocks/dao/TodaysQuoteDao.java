package zmuzik.czechstocks.dao;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import zmuzik.czechstocks.dao.TodaysQuote;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table TODAYS_QUOTE.
*/
public class TodaysQuoteDao extends AbstractDao<TodaysQuote, Long> {

    public static final String TABLENAME = "TODAYS_QUOTE";

    /**
     * Properties of entity TodaysQuote.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Isin = new Property(1, String.class, "isin", false, "ISIN");
        public final static Property Stamp = new Property(2, java.util.Date.class, "stamp", false, "STAMP");
        public final static Property Price = new Property(3, double.class, "price", false, "PRICE");
        public final static Property Volume = new Property(4, double.class, "volume", false, "VOLUME");
    };

    private Query<TodaysQuote> stock_TodaysQuoteListQuery;

    public TodaysQuoteDao(DaoConfig config) {
        super(config);
    }
    
    public TodaysQuoteDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'TODAYS_QUOTE' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'ISIN' TEXT NOT NULL ," + // 1: isin
                "'STAMP' INTEGER NOT NULL ," + // 2: stamp
                "'PRICE' REAL NOT NULL ," + // 3: price
                "'VOLUME' REAL NOT NULL );"); // 4: volume
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'TODAYS_QUOTE'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, TodaysQuote entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getIsin());
        stmt.bindLong(3, entity.getStamp().getTime());
        stmt.bindDouble(4, entity.getPrice());
        stmt.bindDouble(5, entity.getVolume());
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public TodaysQuote readEntity(Cursor cursor, int offset) {
        TodaysQuote entity = new TodaysQuote( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // isin
            new java.util.Date(cursor.getLong(offset + 2)), // stamp
            cursor.getDouble(offset + 3), // price
            cursor.getDouble(offset + 4) // volume
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, TodaysQuote entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setIsin(cursor.getString(offset + 1));
        entity.setStamp(new java.util.Date(cursor.getLong(offset + 2)));
        entity.setPrice(cursor.getDouble(offset + 3));
        entity.setVolume(cursor.getDouble(offset + 4));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(TodaysQuote entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(TodaysQuote entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "todaysQuoteList" to-many relationship of Stock. */
    public List<TodaysQuote> _queryStock_TodaysQuoteList(String isin) {
        synchronized (this) {
            if (stock_TodaysQuoteListQuery == null) {
                QueryBuilder<TodaysQuote> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.Isin.eq(null));
                stock_TodaysQuoteListQuery = queryBuilder.build();
            }
        }
        Query<TodaysQuote> query = stock_TodaysQuoteListQuery.forCurrentThread();
        query.setParameter(0, isin);
        return query.list();
    }

}
