package ensharp.goldensignal;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Semin on 2016-08-23.
 */
public class SharedPreferences {
    //private final String PREF_NAME = "com.rabiaband.pref";

    public final static String PREF_INTRO_USER_AGREEMENT = "PREF_USER_AGREEMENT";
    public final static String PREF_MAIN_VALUE = "PREF_MAIN_VALUE";


    static Context mContext;

    public SharedPreferences(Context c) {
        mContext = c;
    }

    public void putValue(String key, String value, String prefName) {
        android.content.SharedPreferences pref = mContext.getSharedPreferences(prefName,
                Activity.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = pref.edit();

        editor.putString(key, value);
        editor.commit();
    }

    public String getValue(String key, String dftValue, String prefName) {
        android.content.SharedPreferences pref = mContext.getSharedPreferences(prefName,
                Activity.MODE_PRIVATE);
        try {
            return pref.getString(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }
    }

    public void removeAllPreferences(String prefName){
        android.content.SharedPreferences pref = mContext.getSharedPreferences(prefName, Activity.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

}



