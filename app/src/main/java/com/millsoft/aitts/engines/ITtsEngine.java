package com.millsoft.aitts.engines;

import com.millsoft.aitts.global.SpeakTask;

import java.util.Map;
import java.util.TreeMap;

public interface ITtsEngine {
    public String name = "";

    public boolean say(final SpeakTask st);
    Map<String, TtsVoice> voices = new TreeMap<String, TtsVoice>(String.CASE_INSENSITIVE_ORDER);

    public Map<String, TtsVoice> loadVoices();
}
