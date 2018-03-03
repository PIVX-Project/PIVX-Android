package pivx.org.pivxwallet.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import global.store.AbstractDbDao;

/**
 * Created by furszy on 6/6/17.
 */

public abstract class AbstractSqliteDb<T> extends SQLiteOpenHelper implements AbstractDbDao<T> {

    public AbstractSqliteDb(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public AbstractSqliteDb(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }


    public long insert(T obj) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(getTableName(), null, buildContent(obj));
        return id;
    }

    public ArrayList<T> list() {
        ArrayList<T> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+getTableName(), null );
        if(res.moveToFirst()) {
            do {
                list.add(buildFrom(res));
            } while (res.moveToNext());
        }
        return list;
    }

    public Cursor getData(String whereColumn,Object whereObjValue) {
        if (whereObjValue==null) throw new IllegalArgumentException("value cannot be null");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+getTableName()+" where "+whereColumn+"='"+whereObjValue+"'", null );
        return res;
    }

    public T get(String whereColumn,Object whereObjValue) {
        Cursor cursor = getData(whereColumn,whereObjValue);
        if (cursor.moveToFirst()){
            return buildFrom(cursor);
        }
        return null;
    }

    public void updateFieldByKey(String whereColumn,String whereValue, String updateColumn, boolean updateValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(updateColumn,updateValue);
        db.update(getTableName(),contentValues,whereColumn+"=?",new String[]{whereValue});
    }

    public void updateByKey(String whereColumn,String whereValue, T t) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = buildContent(t);
        db.update(getTableName(),contentValues,whereColumn+"=?",new String[]{whereValue});
    }

    public int updateFieldByKey(String whereColumn,String whereValue, String updateColumn, String updateValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(updateColumn,updateValue);
        return db.update(getTableName(),contentValues,whereColumn+"=?",new String[]{whereValue});
    }

    public int numberOfRows(){
        return (int) DatabaseUtils.queryNumEntries(getReadableDatabase(), getTableName());

    }

    public Integer delete(String keyColumn,String columnValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(getTableName(),
                keyColumn+" = ? ",
                new String[] { columnValue });
    }

    protected abstract String getTableName();
    protected abstract ContentValues buildContent(T obj);
    protected abstract T buildFrom(Cursor cursor);


}
