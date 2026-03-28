package com.millsoft.aitts.global;

import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;

/**
 * Params for the background worker
 */
public class SpeakTask {
    public SynthesisRequest request;
    public SynthesisCallback callback;
    Integer id = 0;

    public SpeakTask(SynthesisRequest request, SynthesisCallback callback) {
        this.request = request;
        this.callback = callback;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}