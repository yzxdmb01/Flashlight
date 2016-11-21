package com.jr.flashlight;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016-11-18.
 */

public class SharedPreferencesUtils {
    private static SharedPreferences spf = BaseApplication.getContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
    private static SharedPreferences.Editor editor = spf.edit();

    public static void put(String key, boolean val) {
        editor.putBoolean(key, val);
        editor.commit();
    }

    public static void put(String key, int val) {
        editor.putInt(key, val);
        editor.commit();
    }

    public static int getInt(String key) {
        return spf.getInt(key, -1);
    }

    public static Boolean getBoolean(String key) {
        return spf.getBoolean(key, false);
    }
}
