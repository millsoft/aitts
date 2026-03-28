package com.millsoft.aitts.helper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

public class LanguageHelper {

    private  Map<String, Locale> localeMap;
    private Map<String, String> iso3ToIso2LanguageMap;

    public LanguageHelper() {
        initCountryCodeMapping();
        initLanguageCodeMapping();
    }

    public String iso3CountryCodeToIso2CountryCode(String iso3CountryCode) {
        initCountryCodeMapping();
        if (iso3CountryCode == null || iso3CountryCode.isEmpty()) {
            return "";
        }

        Locale locale = localeMap.get(iso3CountryCode.toUpperCase());
        if (locale != null) {
            return locale.getCountry();
        }

        // Fallback: keep incoming value as-is to avoid crashes.
        return iso3CountryCode;
    }

    public String iso3LanguageCodeToIso2LanguageCode(String iso3LanguageCode) {
        initLanguageCodeMapping();
        if (iso3LanguageCode == null || iso3LanguageCode.isEmpty()) {
            return "";
        }

        String language = iso3ToIso2LanguageMap.get(iso3LanguageCode.toLowerCase());
        if (language != null) {
            return language;
        }

        // Fallback: keep incoming value as-is to avoid crashes.
        return iso3LanguageCode;
    }

    private String iso2CountryCodeToIso3CountryCode(String iso2CountryCode){
        Locale locale = new Locale("", iso2CountryCode.toLowerCase());
        return locale.getISO3Country();
    }

    private void initCountryCodeMapping() {
        String[] countries = Locale.getISOCountries();
        localeMap = new HashMap<String, Locale>(countries.length);
        for (String country : countries) {
            Locale locale = new Locale("", country);
            localeMap.put(locale.getISO3Country().toUpperCase(), locale);
        }
    }

    private void initLanguageCodeMapping() {
        String[] languages = Locale.getISOLanguages();
        iso3ToIso2LanguageMap = new HashMap<String, String>(languages.length);
        for (String language : languages) {
            Locale locale = new Locale(language);
            try {
                iso3ToIso2LanguageMap.put(locale.getISO3Language().toLowerCase(), language.toLowerCase());
            } catch (MissingResourceException ignored) {
                // Keep best-effort mapping only.
            }
        }
    }


    /**
     * Get the selected or default voice for a language
     * @param language
     * @param country
     * @return
     * @deprecated
     */
    public String getVoiceForLanguage(String language, String country){
        String languageCode = "";

        if(language.contains("-")){
            languageCode = language;
        }else {
            if (country.length() == 3) {
                country = iso3CountryCodeToIso2CountryCode(country);
            }

            languageCode = language + "-" + country.toUpperCase();

        }


        String voice = SettingsHelper.getSetting("voice_" + languageCode);
        if(voice.isEmpty()){
            //use default language:
            voice = languageCode + "-Wavenet-A";
        }

        return voice;

    }


    /**
     * returns de-DE if input is deu-DEU
     * @param language
     * @param country
     * @return
     */
    public String parseLanguageCountryCode(String language, String country){
        String languageCode;

        if (language == null || language.isEmpty()) {
            language = "en";
        }

        if (country == null) {
            country = "";
        }

        if(language.contains("-")){
            String[] parts = language.split("-");
            language = parts[0];
            if (country.isEmpty() && parts.length > 1) {
                country = parts[1];
            }
        }

        if (language.length() == 3) {
            language = iso3LanguageCodeToIso2LanguageCode(language);
        }


        if (country.length() == 3) {
            country = iso3CountryCodeToIso2CountryCode(country);
        }

        if (country.isEmpty()) {
            country = "US";
        }

        languageCode = language.toLowerCase() + "-" + country.toUpperCase();

        return languageCode;
    }


}
