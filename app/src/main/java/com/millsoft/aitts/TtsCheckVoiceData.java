/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.millsoft.aitts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*
 * Checks if the voice data is present.
 */
public class TtsCheckVoiceData extends Activity {
    private static final String TAG = "TtsCheckVoiceData";

    private static final String[] SUPPORTED_LANGUAGES = {
            "deu-DEU",
            "dan-DNK",
            "eng-AUS",
            "eng-GBR",
            "eng-IND",
            "eng-USA",
            "ell-GRC",
            "fin-FIN",
            "fra-CAN",
            "fra-FRA",
            "hin-IND",
            "hun-HUN",
            "ind-IDN",
            "ita-ITA",
            "jpn-JPN",
            "kor-KOR",
            "nob-NOR",
            "nld-NLD",
            "pol-POL",
            "por-BRA",
            "por-PRT",
            "rus-RUS",
            "slk-SVK",
            "swe-SWE",
            "tur-TUR",
            "ukr-UKR",
            "vie-VNM",
            "spa-ESP",
            "ces-CZE"

    };

    //"fil-PH",

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        List<String> checkLanguages = getCheckVoiceDataFor(intent);

        // If the call didn't specify which languages to check, check
        // for all the supported ones.
        if (checkLanguages.isEmpty()) {
            checkLanguages = Arrays.asList(SUPPORTED_LANGUAGES);
        }

        ArrayList<String> unavailable = new ArrayList<String>();

        ArrayList<String> available = new ArrayList<String>(checkLanguages);

        int result;

        result = TextToSpeech.Engine.CHECK_VOICE_DATA_PASS;

        // We now return the list of available and unavailable voices
        // as well as the return code.
        Intent returnData = new Intent();

        returnData.putExtra(
                TextToSpeech.Engine.EXTRA_SAMPLE_TEXT, "Hello World");


        returnData.putExtra(
                TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID , "qTTS");



        returnData.putStringArrayListExtra(
                TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES, available);


        returnData.putStringArrayListExtra(
                TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES, unavailable);

        setResult(result, returnData);
        finish();
    }

    /**
     * The intent that launches this activity can contain an intent extra
     * {@link TextToSpeech.Engine.EXTRA_CHECK_VOICE_DATA_FOR} that might specify
     * a given language to check voice data for. If the intent does not contain
     * this extra, we assume that a voice check for all supported languages
     * was requested.
     */
    private List<String> getCheckVoiceDataFor(Intent intent) {
        ArrayList<String> list = intent.getStringArrayListExtra(
                TextToSpeech.Engine.EXTRA_CHECK_VOICE_DATA_FOR);
        ArrayList<String> ret = new ArrayList<String>();
        if (list != null) {
            for (String lang : list) {
                if (!TextUtils.isEmpty(lang)) {
                    ret.add(lang);
                }
            }
        }
        return ret;
    }

    /**
     * Checks whether a given language is in the list of supported languages.
     */
    private boolean isLanguageSupported(String input) {
        for (String lang : SUPPORTED_LANGUAGES) {
            if (lang.equals(input)) {
                return true;
            }
        }

        return false;
    }

    public String LoadData(String inFile) {
        String tContents = "";

        try {
            InputStream stream = getAssets().open(inFile);

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tContents;

    }
}

