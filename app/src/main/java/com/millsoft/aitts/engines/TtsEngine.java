package com.millsoft.aitts.engines;

import android.content.Context;
import android.util.Log;

import com.millsoft.aitts.Settings;
import com.millsoft.aitts.TtsLanguage;
import com.millsoft.aitts.global.SpeakQueue;
import com.millsoft.aitts.global.SpeakTask;
import com.millsoft.aitts.helper.LanguageHelper;
import com.millsoft.aitts.helper.SettingsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TtsEngine implements Runnable {
    public Map<String, BaseTtsEngine> ttsEngines;

    public BaseTtsEngine activeTtsEngine;
    public SpeakQueue _queue;

    private Context context;

    public boolean isRunning = false;
    Thread runnerThread;

    private static final String TAG = "TtsEngine";

    public static final String TTS_ENGINE_DUMMY = "dummy";
    public static final String TTS_ENGINE_GOOGLE = "google";

    private LanguageHelper languageHelper = null;

    //Started from service or settings activity? -> if service, no voice api calls will be performed
    public Boolean isService = false;


    public TtsEngine(Context context, Boolean isService) {

        setContext(context);
        this.isService = isService;

        SettingsHelper.setContext(context);

        languageHelper = new LanguageHelper();

        //Load available languages
        initTtsEngines();
        _queue = new SpeakQueue();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return this.context;
    }

    private void initTtsEngines() {
        ttsEngines = new HashMap<String, BaseTtsEngine>();
        Context ctx = getContext();

        /**
         * GOOGLE TTS
         */
        GoogleTtsEngine googleTtsEngine = new GoogleTtsEngine();
        googleTtsEngine.setContext(ctx);
        ttsEngines.put(TTS_ENGINE_GOOGLE, googleTtsEngine);

        if (!isService) {
            loadEngineVoices();
        }

    }

    private void loadEngineVoices() {
        Log.i(TAG, "Loading Engine Voices");

        for (Map.Entry<String, BaseTtsEngine> ttsEngine : ttsEngines.entrySet()) {
            Log.i(TAG, "Loading Engine Voice for " + ttsEngine.getKey());
            ttsEngine.getValue().loadVoices();
            Log.i(TAG, "Loaded voices count = " + ttsEngine.getValue().getVoiceCount());

        }
    }

    public ArrayList<TtsLanguage> getAllEngineVoices() {
        for (Map.Entry<String, BaseTtsEngine> ttsEngine : ttsEngines.entrySet()) {
            //TODO: merge languages of all engines

            if (ttsEngine.getValue().getVoiceCount() > 0) {
                return ttsEngine.getValue().allLanguages;
            }

        }
        return null;
    }

    public int getAllEngineVoicesCount() {
        int count = 0;
        ArrayList<TtsLanguage> langs = getAllEngineVoices();
        if (langs != null) {
            return langs.size();
        }
        return count;
    }

    public BaseTtsEngine getActiveTts(String langCode) {
        String selectedEngine = Settings.getSetting("engine_" + langCode);
        if (selectedEngine.isEmpty()) {
            return null;
        }

        return ttsEngines.get(selectedEngine);
    }

    public void say(final SpeakTask st) {
        if (isService) {
            speakNow(st);
            return;
        }

        _queue.addToQueue(st);
        runQueue();
    }

    private boolean speakNow(SpeakTask st) {
        String langCode = languageHelper.parseLanguageCountryCode(st.request.getLanguage(), st.request.getCountry());
        BaseTtsEngine selectedEngine = getActiveTts(langCode);
        if (selectedEngine == null) {
            selectedEngine = ttsEngines.get(TTS_ENGINE_GOOGLE);
        }
        if (selectedEngine == null) {
            Log.e(TAG, "No TTS engine available for " + langCode);
            return false;
        }

        return selectedEngine.say(st);
    }

    private void runQueue() {
        if (!isRunning) {
            runnerThread = new Thread(this);
            runnerThread.start();
        }
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        Log.i(TAG, "run:start");
        isRunning = true;
        while (_queue.getCount() > 0) {
            try {
                SpeakTask st = _queue.getSentence();
                speakNow(st);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        isRunning = false;
        Log.i(TAG, "run:end_loop");

    }
}
