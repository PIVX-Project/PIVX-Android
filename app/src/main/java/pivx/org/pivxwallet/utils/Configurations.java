package pivx.org.pivxwallet.utils;

import android.content.SharedPreferences;

/**
 * Created by mati on 22/11/16.
 */

public class Configurations {

    protected final SharedPreferences prefs;

    public Configurations(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public void save(String key,boolean value){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(key, value);
        edit.apply();
    }

    public void save(String key,int value){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(key, value);
        edit.apply();
    }

    public void save(String key,long value){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putLong(key, value);
        edit.apply();
    }

    public void save(String key,String value){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString(key, value);
        edit.apply();
    }

    public boolean getBoolean(String key,boolean defaultValue) {
        return prefs.getBoolean(key,defaultValue);
    }

    public int getInt(String key,int defaultValue) {
        return prefs.getInt(key,defaultValue);
    }

    public long getLong(String key,long defaultValue) {
        return prefs.getLong(key,defaultValue);
    }

    public String getString(String key,String defaultValue) {
        return prefs.getString(key,defaultValue);
    }

    public void remove(){
        prefs.edit().clear().apply();
    }

}
