package com.millsoft.aitts.helper;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SettingsHelper {

    private static final String APP_PREFS_NAME = "TTS";
    private static  Context ctx;


    public static void setContext(Context context){
        ctx = context;
    }

    public static String getSetting(String key)
    {
        SharedPreferences sp = ctx.getSharedPreferences(APP_PREFS_NAME,MODE_PRIVATE);
        String str = sp.getString(key,"");
        return str;
    }

    public static void setSetting(String key, String value)
    {
        SharedPreferences.Editor editor = ctx.getSharedPreferences(APP_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }




}
