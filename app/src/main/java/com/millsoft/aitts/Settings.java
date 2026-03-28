package com.millsoft.aitts;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

import static android.content.Context.MODE_PRIVATE;

public class Settings {

    private static final String APP_PREFS_NAME = "TTS";
    private static  Context ctx;

    public static void setContext(Context context){
        ctx = context;
    }

    public static String getSetting(String key)
    {
        SharedPreferences sp = ctx.getSharedPreferences(APP_PREFS_NAME,MODE_PRIVATE);
        return sp.getString(key,"");
    }

    public static void setSetting(String key, String value)
    {

        SharedPreferences.Editor editor = ctx.getSharedPreferences(APP_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static File getAudioFilePath() {
        File dir = new File(ctx.getCacheDir().toString());

        if (!dir.exists()) {
            dir.mkdir();
        }

        return dir;
    }
}
