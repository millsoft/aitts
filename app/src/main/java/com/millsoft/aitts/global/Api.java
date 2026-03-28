package com.millsoft.aitts.global;


import com.millsoft.aitts.helper.SettingsHelper;

public class Api {

    private final static String BASE_URL = "https://texttospeech.googleapis.com/v1/";
    private static final String TAG = "TTS-Api";


    public static String getApiKey() {
        return SettingsHelper.getSetting(Constants.API_KEY_GOOGLE);
    }

    public static String getApiUrl(String apiMethod) {
        return BASE_URL + apiMethod + "?key=" + getApiKey();
    }

}
