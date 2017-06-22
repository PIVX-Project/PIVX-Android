package pivx.org.pivxwallet.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.KeyIterator;
import com.snappydb.SnappydbException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by furszy on 6/22/17.
 */

public class ContactsStore extends AbstractSqliteDb<Contact>{

    private static final String DATABASE_NAME = "db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "contacts";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ADDRESS = "address";

    private static final int KEY_POS_ID = 0;
    private static final int KEY_POS_NAME = 1;
    private static final int KEY_POS_ADDRESS = 2;


    public ContactsStore(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " +TABLE_NAME+
                        "(" +
                        KEY_ID + " INTEGER primary key autoincrement, "+
                        KEY_NAME + " TEXT, "+
                        KEY_ADDRESS + " TEXT "
                        +")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // todo: this is just for now..
        db.execSQL("DROP TABLE IF EXISTS "+DATABASE_NAME);
        onCreate(db);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    ContentValues buildContent(Contact obj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME,obj.getName());
        contentValues.put(KEY_ADDRESS,obj.getAddresses().get(0));
        return contentValues;
    }

    @Override
    Contact buildFrom(Cursor cursor) {
        int id = cursor.getInt(KEY_POS_ID);
        String name = cursor.getString(KEY_POS_NAME);
        String address = cursor.getString(KEY_POS_ADDRESS);
        Contact contact = new Contact(id,name);
        contact.addAddress(address);
        return contact;
    }
}
