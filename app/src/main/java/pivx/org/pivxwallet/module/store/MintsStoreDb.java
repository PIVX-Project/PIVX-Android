package pivx.org.pivxwallet.module.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zerocoinj.core.CoinDenomination;
import com.zerocoinj.core.accumulators.Accumulator;

import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import host.furszy.zerocoinj.store.AccStoreException;
import host.furszy.zerocoinj.store.coins.MintsStore;
import host.furszy.zerocoinj.store.coins.StoredMint;
import pivx.org.pivxwallet.contacts.AbstractSqliteDb;

/**
 * Created by furszy on 7/5/17.
 */

public class MintsStoreDb extends AbstractSqliteDb<StoredMint> implements MintsStore {


    private static final String DATABASE_NAME = "StoredMints";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_NAME = "mints";

    private static final String KEY_ID = "id";
    private static final String KEY_COMMITMENT_VALUE = "commitment_value";
    private static final String KEY_DENOM = "denom";
    private static final String KEY_COMPUTED_UP_TO_HEIGHT = "computed_up_to_height";
    private static final String KEY_WITNESS_VALUE = "witness_value";

    private static final int KEY_POS_ID = 0;
    private static final int KEY_POS_COMMITMENT_VALUE = 1;
    private static final int KEY_POS_DEMON = 2;
    private static final int KEY_POS_COMPUTED_UP_TO_HEIGHT = 3;
    private static final int KEY_POS_WITNESS_VALUE = 4;


    public MintsStoreDb(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " +TABLE_NAME+
                        "(" +
                        KEY_ID + " INTEGER primary key autoincrement, "+
                        KEY_COMMITMENT_VALUE + " STRING, "+
                        KEY_DENOM + " INTEGER, "+
                        KEY_COMPUTED_UP_TO_HEIGHT + " INTEGER, "+
                        KEY_WITNESS_VALUE + " STRING"
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
    protected ContentValues buildContent(StoredMint obj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_COMMITMENT_VALUE,obj.getCommitmentValue().toString(16));
        contentValues.put(KEY_DENOM,obj.getDenom().getDenomination());
        contentValues.put(KEY_COMPUTED_UP_TO_HEIGHT,obj.getComputedUpToHeight());
        if (obj.getAccWit() != null)
            contentValues.put(KEY_WITNESS_VALUE,obj.getAccWit().toString(16));
        else {
            contentValues.put(KEY_WITNESS_VALUE,"0");
        }
        return contentValues;
    }

    @Override
    protected StoredMint buildFrom(Cursor cursor) {
        BigInteger commitmentValue = new BigInteger(cursor.getString(KEY_POS_COMMITMENT_VALUE), 16);
        CoinDenomination denom = CoinDenomination.fromValue(cursor.getInt(KEY_POS_DEMON));
        int computedUpToHeight = cursor.getInt(KEY_POS_COMPUTED_UP_TO_HEIGHT);
        BigInteger witValue = new BigInteger(cursor.getString(KEY_POS_WITNESS_VALUE), 16);
        if (witValue.equals(BigInteger.ZERO)){
            witValue = null;
        }
        return new StoredMint(
                commitmentValue,
                null,
                denom,
                null,
                -1,
                computedUpToHeight,
                witValue,
                witValue
        );
    }

    @Override
    public boolean put(StoredMint storedMint) {
        return insert(storedMint) != -1;
    }

    @Override
    public StoredMint get(BigInteger commitmentValue) {
        return get(KEY_COMMITMENT_VALUE, commitmentValue.toString(16));
    }

    @Override
    public void deleteStore() {
        truncate();
    }

    @Override
    public boolean update(StoredMint storedMint) {
        int ret = updateByKey(KEY_COMMITMENT_VALUE, storedMint.getCommitmentValue().toString(), storedMint);
        if (ret > 1){
            LoggerFactory.getLogger(MintsStore.class).error("More than one stored mints updated ? " + ret);
        }
        return ret == 1;
    }
}
