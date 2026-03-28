package com.millsoft.aitts.engines;

import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.millsoft.aitts.Settings;
import com.millsoft.aitts.TtsLanguage;
import com.millsoft.aitts.global.Audio;
import com.millsoft.aitts.global.Constants;
import com.millsoft.aitts.global.SpeakTask;
import com.millsoft.aitts.helper.LanguageHelper;
import com.millsoft.aitts.helper.SettingsHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

public class GoogleTtsEngine extends BaseTtsEngine implements ITtsEngine {
    public static final String name = "Google";

    private static final Float GOOGLE_TTS_API_PITCH_MIN = -20f;
    private static final Float GOOGLE_TTS_API_PITCH_MAX = 20f;

    public static String SETTINGS_API_BASE_URL = "https://texttospeech.googleapis.com/v1/";

    private LanguageHelper languageHelper = new LanguageHelper();

    public static String getApiKey() {
        return Settings.getSetting(Constants.API_KEY_GOOGLE);
    }

    public static String getApiUrl(String apiMethod) {
        return SETTINGS_API_BASE_URL + apiMethod + "?key=" + getApiKey();
    }


    /**
     * Say something using the Google API
     *
     * @param st
     * @return
     */
    public boolean say(final SpeakTask st) {


        if (mStopRequested) {
            return false;
        }

        try {

            SynthesisRequest request = st.request;

            String text = request.getCharSequenceText().toString();

            JSONObject jsonBody = new JSONObject();

            JSONObject jsonAudioConfig = new JSONObject();
            jsonAudioConfig.put("audioEncoding", "LINEAR16");

            //speech rate android is between 10 and 600.
            //google tts api between 0.25 and 4, where 1 = normal voice (100%)
            //reset:  speed = 100
            //so we need to convert it first.

            int rateAndroid = request.getSpeechRate();
            int pitchAndroid = request.getPitch();


            double apiSpeekingRate = (double) rateAndroid / 100;
            float apiSpeekingPitch = 0;

            if (apiSpeekingRate > 4) {
                //max is 4
                apiSpeekingRate = 4;
            }

            if (apiSpeekingRate < 0.25) {
                //max is 4
                apiSpeekingRate = 0.25;
            }


            float percentPitch;

            if (pitchAndroid > 100) {
                //Positive

                //calculate percents:
                percentPitch = (float) pitchAndroid * 100 / 400;    //zb 0 - 100%
                apiSpeekingPitch = percentPitch * GOOGLE_TTS_API_PITCH_MAX / 100;

            } else if (pitchAndroid < 100) {
                //Negative:
                //min is 25 so max is 100-25
                pitchAndroid = 100 - pitchAndroid; //1...2...75
                percentPitch = (float) pitchAndroid * 100 / 75;    //zb 0 - 100%
                apiSpeekingPitch = percentPitch * (GOOGLE_TTS_API_PITCH_MIN / 100);


            } else {
                pitchAndroid = 0;
            }

            jsonAudioConfig.put("speakingRate", apiSpeekingRate);
            jsonAudioConfig.put("pitch", apiSpeekingPitch);


            JSONObject jsonInput = new JSONObject();
            if (text.contains("<speak")) {
                //SSML
                jsonInput.put("ssml", text);

            } else {
                //Normal Text:
                jsonInput.put("text", text);
            }


            JSONObject jsonVoice = new JSONObject();

            String languageCode = "en-US";

            String gender = "FEMALE";

            String langCode = languageHelper.parseLanguageCountryCode(st.request.getLanguage(), st.request.getCountry());

            String voice = SettingsHelper.getSetting("voice_" + langCode + "_" + name.toLowerCase());
            if (voice.isEmpty()) {
                //use default language:
                voice = langCode + "-Wavenet-A";
            }

            jsonVoice.put("languageCode", langCode);
            jsonVoice.put("name", voice);  //Stimme
            jsonVoice.put("ssmlGender", gender);

            jsonBody.put("audioConfig", jsonAudioConfig);
            jsonBody.put("input", jsonInput);
            jsonBody.put("voice", jsonVoice);

            RequestFuture<JSONObject> future = RequestFuture.newFuture();

            RequestQueue queue = Volley.newRequestQueue(this.getContext());
            String url = getApiUrl("text:synthesize");

            Log.i("google:say", st.request.getCharSequenceText().toString());

            JsonObjectRequest apiRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    future, future
            );

            queue.add(apiRequest);
            try {

                JSONObject response = future.get();

                try {

                    Audio.DecodedAudio decodedAudio = new Audio(getContext()).getPcmFromBase64Wav(response.get("audioContent").toString());
                    byte[] pcmBytes = decodedAudio.pcmData;
                    int byteLength = pcmBytes.length;

                    if (st.callback.start(decodedAudio.sampleRateHz, decodedAudio.audioFormat, decodedAudio.channelCount) != TextToSpeech.SUCCESS) {
                        Log.e("google:say", "start ERROR");
                        st.callback.error();
                        return false;
                    }

                    final int maxBufferSize = st.callback.getMaxBufferSize();
                    final int bytesPerSample = decodedAudio.audioFormat == android.media.AudioFormat.ENCODING_PCM_8BIT ? 1 : 2;
                    final int frameSize = Math.max(1, decodedAudio.channelCount * bytesPerSample);
                    int offset = 0;
                    int frameId = 1;

                    while (offset < byteLength) {
                        int bytesToWrite = Math.min(maxBufferSize, byteLength - offset);
                        if (bytesToWrite > frameSize) {
                            bytesToWrite -= bytesToWrite % frameSize;
                        }
                        if (bytesToWrite <= 0) {
                            bytesToWrite = Math.min(frameSize, byteLength - offset);
                        }
                        Log.i("google:say", "pre_audioAvailable" + frameId + " offset = " + offset + "/" + byteLength);
                        if (st.callback.audioAvailable(pcmBytes, offset, bytesToWrite) != TextToSpeech.SUCCESS) {
                            Log.e("google:say", "audioAvailable ERROR!" + frameId);
                            st.callback.error();
                            return false;
                        }
                        Log.i("google:say", "post_audioAvailable" + frameId);
                        offset += bytesToWrite;
                        frameId++;
                    }

                    st.callback.done();

                } catch (JSONException e) {
                    Log.i("google:say", "ERROR");

                    e.printStackTrace();
                    st.callback.error();
                }

            } catch (InterruptedException | ExecutionException e) {
                st.callback.error();
            }


            return true;

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return false;
    }


    public Map<String, TtsVoice> loadVoices() {

        log("loading voices for google");
        if (!isApiKeyValid()) {
            return null;
        }

        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        String url = getApiUrl("voices");

        RequestFuture<JSONObject> jsonObjectRequestFuture = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, (JSONObject) null, jsonObjectRequestFuture, jsonObjectRequestFuture);
        jsonObjectRequestFuture.setRequest(request);
        queue.add(request);

        try {
            JSONObject response = jsonObjectRequestFuture.get(10, TimeUnit.SECONDS);
            JSONArray voices = response.getJSONArray("voices");

            allLanguages = new ArrayList<TtsLanguage>();
            log("Google Voices Count " + voices.length());
            log(voices.toString());

            for (int i = 0; i < voices.length(); i++) {
                JSONObject currentLang = voices.getJSONObject(i);

                JSONArray _langCodeCode = currentLang.getJSONArray("languageCodes");
                String voiceName = currentLang.getString("name");
                String voiceGender = currentLang.getString("ssmlGender");

                String currentLanguage = _langCodeCode.getString(0);

                for (Locale locale : Locale.getAvailableLocales()) {

                    if (locale.toLanguageTag().equalsIgnoreCase(currentLanguage)) {
                        allLanguages.add(new TtsLanguage(name.toLowerCase(), locale.toLanguageTag(), locale.getDisplayName(), voiceName, voiceGender));
                        break;
                    }
                }

            }

        } catch (InterruptedException | TimeoutException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }

        log("loaded google voices");
        return null;
    }

    public boolean isApiKeyValid() {

        //after check routine, this will be set to true if the key is valid
        boolean isApiKeyValid = false;

        if (getApiKey().length() == 0) {
            log("no google key available");
            return false;
        }

        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        String url = getApiUrl("voices");

        RequestFuture<JSONObject> jsonObjectRequestFuture = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, (JSONObject) null, jsonObjectRequestFuture, jsonObjectRequestFuture);
        jsonObjectRequestFuture.setRequest(request);
        queue.add(request);

        try {
            log("wait a moment");

            jsonObjectRequestFuture.get(10, TimeUnit.SECONDS);
            //no errors, we assume key was good
            isApiKeyValid = true;

            log("seems good");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        return isApiKeyValid;
    }
}
