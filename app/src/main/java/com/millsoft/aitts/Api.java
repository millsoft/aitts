package com.millsoft.aitts;

import com.millsoft.aitts.global.Constants;

public class Api {

    private final static String BASE_URL = "https://texttospeech.googleapis.com/v1/";

    public static String getApiKey() {
        return Settings.getSetting(Constants.API_KEY_GOOGLE);
    }

    public static String getApiUrl(String apiMethod) {
        return BASE_URL + apiMethod + "?key=" + getApiKey();
    }

}
