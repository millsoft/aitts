package com.millsoft.aitts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import com.millsoft.aitts.global.Constants;


/**
 * Get the sample text when user clicks on "PLAY" in the tts setting activity
 */

public class TtsGetSampleText extends Activity {
    private static final String TAG = "TtsGetSampleText";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        String language = intent.getStringExtra("language");
        String country = intent.getStringExtra("country");
        String variant = intent.getStringExtra("variant");

        int result;
        result = 0;

        // We now return the list of available and unavailable voices
        // as well as the return code.
        Intent returnData = new Intent();

        returnData.putExtra(
                TextToSpeech.Engine.EXTRA_SAMPLE_TEXT, Constants.TTS_TEST_SAMPLE_TEXT);


        setResult(result, returnData);
        finish();
    }

}
