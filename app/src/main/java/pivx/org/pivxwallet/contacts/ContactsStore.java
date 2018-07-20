package pivx.org.pivxwallet.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.pivxj.core.Sha256Hash;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import global.AddressLabel;
import global.store.ContactsStoreDao;

/**
 * Created by furszy on 6/22/17.
 */

public class ContactsStore extends AbstractSqliteDb<AddressLabel> implements ContactsStoreDao<AddressLabel> {

    private static final String DATABASE_NAME = "db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "contacts";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_TX_HASHES_SET = "tx_hashes_set";

    private static final int KEY_POS_ID = 0;
    private static final int KEY_POS_NAME = 1;
    private static final int KEY_POS_ADDRESS = 2;
    private static final int KEY_POS_TX_HASHES_SET = 3;


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
                        KEY_ADDRESS + " TEXT, "+
                        KEY_TX_HASHES_SET + " TEXT "
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
    protected ContentValues buildContent(AddressLabel obj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME,obj.getName());
        contentValues.put(KEY_ADDRESS,obj.getAddresses().get(0));
        if (obj.getTxHashes()!=null)
            contentValues.put(KEY_TX_HASHES_SET,toJson(obj.getTxHashes()));
        return contentValues;
    }

    @Override
    protected AddressLabel buildFrom(Cursor cursor) {
        try {
            int id = cursor.getInt(KEY_POS_ID);
            String name = cursor.getString(KEY_POS_NAME);
            String address = cursor.getString(KEY_POS_ADDRESS);
            String txHashesJson = cursor.getString(KEY_POS_TX_HASHES_SET);
            Set<String> txHashes = null;
            if (txHashesJson != null)
                txHashes = fromJson(txHashesJson);
            AddressLabel addressLabel = new AddressLabel(id, name);
            addressLabel.addAddress(address);
            addressLabel.addAllTx(txHashes);
            return addressLabel;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new CantBuildContactException(e);
        }
    }

    public AddressLabel getContact(String address){
        return get(KEY_ADDRESS,address);
    }

    /**
     * Add tx hash to a contact
     *
     * @param addressContact
     * @param txHash
     * @return
     */
    public boolean addTxHash(String addressContact, Sha256Hash txHash){
        AddressLabel addressLabel = getContact(addressContact);
        addressLabel.addTx(txHash);
        return updateFieldByKey(KEY_ADDRESS,addressContact,KEY_TX_HASHES_SET,toJson(addressLabel.getTxHashes()))==1;
    }

    public String toJson(Set<String> set){
        JSONArray jsonArray = new JSONArray();
        for (String s : set) {
            jsonArray.put(s);
        }
        return jsonArray.toString();
    }

    public Set<String> fromJson(String json) throws JSONException {
        Set<String> set = new HashSet<>();
        JSONArray jsonArray = new JSONArray(json);
        for (int i=0;i<jsonArray.length();i++){
            set.add(jsonArray.getString(i));
        }
        return set;
    }

    public void delete(AddressLabel data) {
        delete(KEY_ADDRESS,data.getAddresses().get(0));
    }

    // TODO: Check this..
    @Override
    public List<AddressLabel> getMyAddresses() {
        return null;
    }

    @Override
    public List<AddressLabel> getContacts() {
        return list();
    }
}
