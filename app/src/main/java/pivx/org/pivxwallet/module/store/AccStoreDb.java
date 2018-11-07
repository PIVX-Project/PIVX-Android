package pivx.org.pivxwallet.module.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zerocoinj.core.CoinDenomination;
import com.zerocoinj.core.accumulators.Accumulator;

import java.math.BigDecimal;
import java.math.BigInteger;

import host.furszy.zerocoinj.store.AccStore;
import host.furszy.zerocoinj.store.AccStoreException;
import pivx.org.pivxwallet.contacts.AbstractSqliteDb;
import global.PivxRate;
import global.store.RateDbDao;

/**
 * Created by furszy on 7/5/17.
 */

public class AccStoreDb extends AbstractSqliteDb<StoredAccumulator> implements AccStore {


    private static final String DATABASE_NAME = "AccStore";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "acc";

    private static final String KEY_ID = "id";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_DENOM = "denom";
    private static final String KEY_VALUE = "value";

    private static final int KEY_POS_ID = 0;
    private static final int KEY_POS_HEIGHT = 1;
    private static final int KEY_POS_DEMON = 2;
    private static final int KEY_POS_VALUE = 3;


    public AccStoreDb(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " +TABLE_NAME+
                        "(" +
                        KEY_ID + " INTEGER primary key autoincrement, "+
                        KEY_HEIGHT + " INTEGER, "+
                        KEY_DENOM + " INTEGER, "+
                        KEY_VALUE + " TEXT"
                        +")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // todo: this is just for now..
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected ContentValues buildContent(StoredAccumulator obj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_HEIGHT,obj.getHeight());
        contentValues.put(KEY_DENOM,obj.getDenom().getDenomination());
        contentValues.put(KEY_VALUE,obj.getValue().toString());
        return contentValues;
    }

    @Override
    protected StoredAccumulator buildFrom(Cursor cursor) {
        int height = cursor.getInt(KEY_POS_HEIGHT);
        BigInteger value = new BigInteger(cursor.getString(KEY_POS_VALUE));
        CoinDenomination denom = CoinDenomination.fromValue(cursor.getInt(KEY_POS_DEMON));
        return new StoredAccumulator(height,denom,value);
    }

    @Override
    public void put(int height, Accumulator accumulator) throws AccStoreException {
        long ret = insert(new StoredAccumulator(height,accumulator.getDenomination(), accumulator.getValue()));
        if (ret == -1) throw new AccStoreException("Cannot store acc for " + height + ", and denom: " + accumulator.getDenomination(), height, accumulator.getDenomination());
        Log.e("AccStoreDb" ,"ret: " +ret);
    }

    @Override
    public BigInteger get(int height, CoinDenomination coinDenomination) throws AccStoreException {
        Cursor cursor = getReadableDatabase().rawQuery( "select * from "+getTableName()+" where "+KEY_HEIGHT+"='"+String.valueOf(height)+"' AND " +
                KEY_DENOM+"='"+coinDenomination.getDenomination()+"'", null );

        if(cursor.moveToFirst()) {
            StoredAccumulator stored = buildFrom(cursor);
            if (stored.getDenom() == coinDenomination && stored.getHeight() == height){
                return stored.getValue();
            }
        }
        return null;

    }
}
