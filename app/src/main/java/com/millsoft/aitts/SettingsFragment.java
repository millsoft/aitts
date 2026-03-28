package com.millsoft.aitts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.millsoft.aitts.global.Audio;
import com.millsoft.aitts.global.Constants;
import com.millsoft.aitts.engines.TtsEngine;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class SettingsFragment extends Fragment implements View.OnClickListener {
    private TextToSpeech tts;

    private View thisView;
    //listview voices:
    public ListView listView;
    public ArrayAdapter<TtsLanguage> ttsLanguageList;

    private ArrayList<TtsLanguage> allLanguages;
    private static final String TAG = "TTS-SettingsFrag";
    private static final String TTS_DEFAULT_LOCALE_KEY = "tts_default_locale";
    private final Object previewAudioLock = new Object();
    private AudioTrack previewAudioTrack;
    private Thread previewThread;


    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        thisView = inflater.inflate(R.layout.fragment_settings, container, false);

        Button b = (Button) thisView.findViewById(R.id.btnApiKey);
        b.setOnClickListener(this);

        loadVoicesListview();

        return thisView;
    }

    public ArrayList<TtsLanguage> getAllEngineVoices() {
        TtsSettingsActivity activity = (TtsSettingsActivity) getActivity();
        if (activity == null) {
            return new ArrayList<TtsLanguage>();
        }

        TtsEngine engine = activity.ttsEngine;
        if (engine == null) {
            return new ArrayList<TtsLanguage>();
        }

        ArrayList<TtsLanguage> voices = engine.getAllEngineVoices();
        if (voices == null) {
            return new ArrayList<TtsLanguage>();
        }

        return voices;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnApiKey:
                ((TtsSettingsActivity) getActivity()).showApiKeyDialog();
                break;
        }

    }

    /**
     * Show / Hide tts settings options
     * Will show the voice list and other things if the api is available (eg the api key was entered)
     *
     * @param isAvailable
     */
    public void setApiIsAvailable(Boolean isAvailable) {
        final TextView textSelectVoice = getActivity().findViewById(R.id.txtInfoVoice);
        final Button btnTest = getActivity().findViewById(R.id.btnTest);

        if (isAvailable) {
            btnTest.setVisibility(View.VISIBLE);
            textSelectVoice.setVisibility(View.VISIBLE);
        } else {
            btnTest.setVisibility(View.INVISIBLE);
            textSelectVoice.setVisibility(View.INVISIBLE);
        }
    }


    public void loadVoicesListview() {
        listView = thisView.findViewById(R.id.listView);

        ArrayList<TtsLanguage> allEngineVoices = getAllEngineVoices();

        //Sort languages by name
        Collections.sort(allEngineVoices, new Comparator<TtsLanguage>() {
            public int compare(TtsLanguage obj1, TtsLanguage obj2) {
                return obj1.getLanguageName().compareToIgnoreCase(obj2.getLanguageName());
            }
        });


        ArrayList<TtsLanguage> languagesOnly = new ArrayList<TtsLanguage>();
        for (TtsLanguage lang : allEngineVoices) {

            if (!isLanguageInList(languagesOnly, lang.getLangCode())) {
                languagesOnly.add(lang);
            }

        }

        String activeLangCode = getCurrentSelectedLanguageCode();
        int selectedPosition = findSelectedLanguagePosition(languagesOnly, activeLangCode);
        if (selectedPosition > 0) {
            TtsLanguage selectedLanguage = languagesOnly.remove(selectedPosition);
            languagesOnly.add(0, selectedLanguage);
            selectedPosition = 0;
        }

        final int highlightedPosition = selectedPosition;
        ttsLanguageList
                = new ArrayAdapter<TtsLanguage>(getActivity(), R.layout.language_list_item, languagesOnly) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                boolean isHighlighted = highlightedPosition >= 0 && position == highlightedPosition;
                row.setActivated(isHighlighted);
                row.setSelected(isHighlighted);
                return row;
            }
        };
        listView.setAdapter(ttsLanguageList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TtsLanguage lang = (TtsLanguage) adapterView.getItemAtPosition(i);
                showVoiceDialog(lang);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (thisView != null) {
            loadVoicesListview();
        }
    }

    /**
     * Show the voice select dialog in the tts settings
     *
     * @param lang
     */
    private void showVoiceDialog(final TtsLanguage lang) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getText(R.string.select_voice_for) + " " + lang.getLanguageName());

        final ArrayList<TtsLanguage> voicesForLanguage = new ArrayList<TtsLanguage>();

        String selectedEngine = Settings.getSetting("engine_" + lang.getLangCode());
        String selectedLangVoice = Settings.getSetting("voice_" + lang.getLangCode() + "_" + selectedEngine);

        int checkedItem = 0;
        int indexVoice = 0;

        for (TtsLanguage lng : getAllEngineVoices()) {

            if (lng.getLangCode().equalsIgnoreCase(lang.getLangCode())) {
                TtsLanguage currentLang = new TtsLanguage(lng.getEngineName(), lng.getLangCode(), lng.getLanguageName(), lng.getVoice(), lng.getGender());
                currentLang.isVoice = true;
                voicesForLanguage.add(currentLang);

                if (selectedLangVoice.equals(currentLang.getVoice())) {
                    checkedItem = indexVoice;
                }

                indexVoice++;
            }
        }

        final ExtendedArrayAdapter adapter = new ExtendedArrayAdapter(getActivity(), voicesForLanguage, checkedItem, new ExtendedArrayAdapter.OnPreviewClickListener() {
            @Override
            public void onPreviewClick(TtsLanguage language) {
                previewVoice(language);
            }
        });

        ListView dialogListView = new ListView(getActivity());
        dialogListView.setAdapter(adapter);
        dialogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedPosition(position);
            }
        });

        builder.setView(dialogListView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                TtsLanguage selectedLang = adapter.getItem(adapter.getSelectedPosition());
                if (selectedLang == null) {
                    return;
                }

                Settings.setSetting("voice_" + lang.getLangCode() + "_" + selectedLang.getEngineName().toLowerCase(), selectedLang.getVoice());
                Settings.setSetting("engine_" + lang.getLangCode(), selectedLang.getEngineName().toLowerCase());
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                stopPreviewPlayback();
            }
        });
        dialog.show();
    }

    private void previewVoice(final TtsLanguage language) {
        if (getActivity() == null) {
            return;
        }

        stopPreviewPlayback();

        final android.content.Context appContext = getActivity().getApplicationContext();
        previewThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonBody = new JSONObject();

                    JSONObject jsonAudioConfig = new JSONObject();
                    jsonAudioConfig.put("audioEncoding", "LINEAR16");

                    JSONObject jsonInput = new JSONObject();
                    jsonInput.put("text", Constants.TTS_TEST_SAMPLE_TEXT);

                    JSONObject jsonVoice = new JSONObject();
                    jsonVoice.put("languageCode", language.getLangCode());
                    jsonVoice.put("name", language.getVoice());
                    jsonVoice.put("ssmlGender", language.getGender());

                    jsonBody.put("audioConfig", jsonAudioConfig);
                    jsonBody.put("input", jsonInput);
                    jsonBody.put("voice", jsonVoice);

                    RequestFuture<JSONObject> future = RequestFuture.newFuture();
                    RequestQueue queue = Volley.newRequestQueue(appContext);

                    JsonObjectRequest apiRequest = new JsonObjectRequest(Request.Method.POST, Api.getApiUrl("text:synthesize"), jsonBody, future, future);
                    queue.add(apiRequest);

                    JSONObject response = future.get(10, TimeUnit.SECONDS);
                    Audio.DecodedAudio decodedAudio = new Audio(appContext).getPcmFromBase64Wav(response.getString("audioContent"));
                    playPreviewAudio(decodedAudio);
                } catch (Exception e) {
                    Log.e(TAG, "Voice preview failed", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), R.string.voice_preview_failed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        });
        previewThread.start();
    }

    private void playPreviewAudio(Audio.DecodedAudio decodedAudio) {
        int channelConfig = decodedAudio.channelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
        int bytesPerSample = decodedAudio.audioFormat == AudioFormat.ENCODING_PCM_8BIT ? 1 : 2;
        int bytesPerFrame = Math.max(1, decodedAudio.channelCount * bytesPerSample);
        int pcmLength = decodedAudio.pcmData.length - (decodedAudio.pcmData.length % bytesPerFrame);
        if (pcmLength <= 0) {
            return;
        }

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        AudioFormat audioFormat = new AudioFormat.Builder()
                .setSampleRate(decodedAudio.sampleRateHz)
                .setEncoding(decodedAudio.audioFormat)
                .setChannelMask(channelConfig)
                .build();

        AudioTrack audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(pcmLength)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build();

        synchronized (previewAudioLock) {
            previewAudioTrack = audioTrack;
        }

        try {
            int written = audioTrack.write(decodedAudio.pcmData, 0, pcmLength);
            if (written <= 0) {
                return;
            }
            audioTrack.play();

            int totalFrames = written / bytesPerFrame;
            while (!Thread.currentThread().isInterrupted() && audioTrack.getPlaybackHeadPosition() < totalFrames) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            try {
                audioTrack.stop();
            } catch (IllegalStateException ignored) {
            }
            audioTrack.release();

            synchronized (previewAudioLock) {
                if (previewAudioTrack == audioTrack) {
                    previewAudioTrack = null;
                }
            }
        }
    }

    private void stopPreviewPlayback() {
        Thread runningPreviewThread = previewThread;
        if (runningPreviewThread != null && runningPreviewThread.isAlive()) {
            runningPreviewThread.interrupt();
        }
        previewThread = null;

        synchronized (previewAudioLock) {
            if (previewAudioTrack != null) {
                try {
                    previewAudioTrack.pause();
                    previewAudioTrack.flush();
                    previewAudioTrack.stop();
                } catch (IllegalStateException ignored) {
                }
                previewAudioTrack.release();
                previewAudioTrack = null;
            }
        }
    }

    @Override
    public void onDestroyView() {
        stopPreviewPlayback();
        super.onDestroyView();
    }

    private static boolean isLanguageInList(ArrayList<TtsLanguage> list, String lang) {
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

    private String getCurrentSelectedLanguageCode() {
        Locale systemLocale = Locale.getDefault();
        String systemLangCode = normalizeLanguageCode(systemLocale.toLanguageTag(), systemLocale);

        if (getActivity() == null) {
            return systemLangCode;
        }

        String packageName = getActivity().getPackageName();
        String defaultLocaleConfig = android.provider.Settings.Secure.getString(
                getActivity().getContentResolver(),
                TTS_DEFAULT_LOCALE_KEY
        );

        String selectedLocale = getEngineLocaleFromTtsConfig(defaultLocaleConfig, packageName);
        if (selectedLocale.isEmpty() || "system".equalsIgnoreCase(selectedLocale)) {
            return systemLangCode;
        }

        return normalizeLanguageCode(selectedLocale, systemLocale);
    }

    private String getEngineLocaleFromTtsConfig(String ttsDefaultLocaleConfig, String packageName) {
        if (ttsDefaultLocaleConfig == null || ttsDefaultLocaleConfig.isEmpty()) {
            return "";
        }

        String[] entries = ttsDefaultLocaleConfig.split(",");
        for (String entry : entries) {
            if (entry == null || entry.isEmpty()) {
                continue;
            }

            String[] parts = entry.split(":", 2);
            if (parts.length == 2 && packageName.equals(parts[0])) {
                return parts[1];
            }
        }

        return "";
    }

    private String normalizeLanguageCode(String localeString, Locale fallbackLocale) {
        if (localeString == null) {
            localeString = "";
        }

        String normalized = localeString.replace('_', '-').trim();
        String language = fallbackLocale.getLanguage();
        String country = fallbackLocale.getCountry();

        if (!normalized.isEmpty()) {
            String[] parts = normalized.split("-");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                language = parts[0];
            }
            if (parts.length > 1 && isCountryPart(parts[1])) {
                country = parts[1];
            } else if (parts.length > 2 && isCountryPart(parts[2])) {
                country = parts[2];
            }
        }

        com.millsoft.aitts.helper.LanguageHelper languageHelper = new com.millsoft.aitts.helper.LanguageHelper();
        return languageHelper.parseLanguageCountryCode(language, country);
    }

    private boolean isCountryPart(String value) {
        return value != null && (value.length() == 2 || value.length() == 3);
    }

    private int findSelectedLanguagePosition(ArrayList<TtsLanguage> languages, String selectedLangCode) {
        if (languages == null || selectedLangCode == null || selectedLangCode.isEmpty()) {
            return -1;
        }

        int firstLanguageMatch = -1;
        String selectedLanguageOnly = selectedLangCode.split("-")[0].toLowerCase();

        for (int i = 0; i < languages.size(); i++) {
            TtsLanguage language = languages.get(i);
            if (language == null || language.getLangCode() == null) {
                continue;
            }

            String candidate = language.getLangCode();
            if (candidate.equalsIgnoreCase(selectedLangCode)) {
                return i;
            }

            String[] parts = candidate.split("-");
            if (firstLanguageMatch < 0 && parts.length > 0 && parts[0].equalsIgnoreCase(selectedLanguageOnly)) {
                firstLanguageMatch = i;
            }
        }

        return firstLanguageMatch;
    }

}
