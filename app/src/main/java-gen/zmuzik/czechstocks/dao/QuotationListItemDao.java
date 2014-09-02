package zmuzik.czechstocks.dao;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;

import zmuzik.czechstocks.dao.QuotationListItem;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table QUOTATION_LIST_ITEM.
*/
public class QuotationListItemDao extends AbstractDao<QuotationListItem, String> {

    public static final String TABLENAME = "QUOTATION_LIST_ITEM";

    /**
     * Properties of entity QuotationListItem.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Isin = new Property(0, String.class, "isin", true, "ISIN");
    };

    private DaoSession daoSession;


    public QuotationListItemDao(DaoConfig config) {
        super(config);
    }
    
    public QuotationListItemDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'QUOTATION_LIST_ITEM' (" + //
                "'ISIN' TEXT PRIMARY KEY NOT NULL );"); // 0: isin
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'QUOTATION_LIST_ITEM'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, QuotationListItem entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getIsin());
    }

    @Override
    protected void attachEntity(QuotationListItem entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public QuotationListItem readEntity(Cursor cursor, int offset) {
        QuotationListItem entity = new QuotationListItem( //
            cursor.getString(offset + 0) // isin
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, QuotationListItem entity, int offset) {
        entity.setIsin(cursor.getString(offset + 0));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(QuotationListItem entity, long rowId) {
        return entity.getIsin();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(QuotationListItem entity) {
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
    
    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getCurrentTradingDataDao().getAllColumns());
            builder.append(" FROM QUOTATION_LIST_ITEM T");
            builder.append(" LEFT JOIN CURRENT_TRADING_DATA T0 ON T.'ISIN'=T0.'ISIN'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected QuotationListItem loadCurrentDeep(Cursor cursor, boolean lock) {
        QuotationListItem entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        CurrentTradingData currentTradingData = loadCurrentOther(daoSession.getCurrentTradingDataDao(), cursor, offset);
         if(currentTradingData != null) {
            entity.setCurrentTradingData(currentTradingData);
        }

        return entity;    
    }

    public QuotationListItem loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<QuotationListItem> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<QuotationListItem> list = new ArrayList<QuotationListItem>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<QuotationListItem> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<QuotationListItem> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}