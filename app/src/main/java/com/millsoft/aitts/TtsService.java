/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.millsoft.aitts;

import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.util.Log;


import com.millsoft.aitts.engines.TtsEngine;
import com.millsoft.aitts.global.SpeakTask;

/**
 * A text to speech engine that generates "speech" that a robot might understand.
 * The engine supports two different "languages", each with their own frequency
 * mappings.
 * <p>
 * It exercises all aspects of the Text to speech engine API
 * {@link TextToSpeechService}.
 */
public class TtsService extends TextToSpeechService {
    private static final String TAG = "TTS";

    private volatile String[] mCurrentLanguage = null;
    private volatile boolean mStopRequested = false;

    private int CountTask = 0;

    public String currentCountry = "";

    public TtsEngine ttsEngine = null;


    @Override
    public void onCreate() {
        super.onCreate();

        Settings.setContext(getApplicationContext());


        // We load the default language when we start up. This isn't strictly
        // required though, it can always be loaded lazily on the first call to
        // onLoadLanguage or onSynthesizeText. This a tradeoff between memory usage
        // and the latency of the first call.
        onLoadLanguage("en", "us", "");

        ttsEngine = new TtsEngine(getApplicationContext(), true);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected String[] onGetLanguage() {
        // Note that mCurrentLanguage is volatile because this can be called from
        // multiple threads.
        return mCurrentLanguage;
    }

    @Override
    protected int onIsLanguageAvailable(String lang, String country, String variant) {

        this.currentCountry = country;
        return TextToSpeech.LANG_COUNTRY_AVAILABLE;

    }


    /*
     * Note that this method is synchronized, as is onSynthesizeText because
     * onLoadLanguage can be called from multiple threads (while onSynthesizeText
     * is always called from a single thread only).
     */
    @Override
    protected synchronized int onLoadLanguage(String lang, String country, String variant) {

        final int isLanguageAvailable = onIsLanguageAvailable(lang, country, variant);

        Log.i(TAG, "*** isLanguageAvailable : " + isLanguageAvailable);

        Log.i(TAG, "*** Lang    : " + lang);
        Log.i(TAG, "*** Country : " + country);
        Log.i(TAG, "*** Variant : " + variant);

        if (isLanguageAvailable == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.i(TAG, "*** LANG_NOT_SUPPORTED");
            return isLanguageAvailable;
        }

        String loadCountry = country;
        if (isLanguageAvailable == TextToSpeech.LANG_AVAILABLE) {
            Log.i(TAG, "*** LANG_AVAILABLE !");
            loadCountry = "US";
        }

        // If we've already loaded the requested language, we can return early.
        if (mCurrentLanguage != null) {
            if (mCurrentLanguage[0].equals(lang) && mCurrentLanguage[1].equals(country)) {
                return isLanguageAvailable;
            }
        }

        Log.i(TAG, "*** almost done with onLoadLanguage");

        mCurrentLanguage = new String[]{lang, loadCountry, ""};

        return isLanguageAvailable;
    }

    @Override
    protected void onStop() {
        mStopRequested = true;
        if (ttsEngine != null && ttsEngine._queue != null) {
            ttsEngine._queue.clearQueue();
        }
        mStopRequested = false;
    }


    @Override
    protected synchronized void onSynthesizeText(SynthesisRequest request,
                                                 SynthesisCallback callback) {

        SpeakTask st = new SpeakTask(request, callback);
        st.setId(++CountTask);

        ttsEngine.say(st);

    }

}
