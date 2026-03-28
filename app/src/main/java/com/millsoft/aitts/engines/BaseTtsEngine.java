package com.millsoft.aitts.engines;

import android.content.Context;
import android.util.Log;

import com.millsoft.aitts.TtsLanguage;
import com.millsoft.aitts.global.SpeakTask;

import java.util.ArrayList;
import java.util.Map;

public class BaseTtsEngine implements ITtsEngine{
    private Context context;
    public volatile boolean mStopRequested = false;

    public String name = "BaseTtsEngine";

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void log(String message){
        Log.i(this.name, message);
    }

    public ArrayList<TtsLanguage> allLanguages;

    @Override
    public boolean say(SpeakTask st) {
        return false;
    }

    @Override
    public Map<String, TtsVoice> loadVoices() {
        return null;
    }

    public int getVoiceCount() {
        int count = 0;
        if(allLanguages != null){
            count = allLanguages.size();
        }
        return count;
    }

    public static boolean isLanguageInList(ArrayList<TtsLanguage> list, String lang) {
        if (list == null) {
            return false;
        }

        for (TtsLanguage item : list) {

            if (item.getLangCode().equalsIgnoreCase(lang)) {
                return true;
            }
        }
        return false;
    }

}
