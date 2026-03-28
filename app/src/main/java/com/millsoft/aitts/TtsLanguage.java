package com.millsoft.aitts;

import java.io.Serializable;

public class TtsLanguage implements Serializable {

    private String engineName;
    private String langCode;
    private String languageName;
    private String voice = "";
    private String gender;

    //if voice, then only voice will be returned in toString
    public Boolean isVoice = false;

    public TtsLanguage(String engineName, String langCode, String languageName) {
        this.engineName = engineName;
        this.langCode = langCode;
        this.languageName = languageName;
    }

    public TtsLanguage(String engineName, String langCode, String languageName, String voice) {
        this.engineName = engineName;
        this.langCode = langCode;
        this.languageName = languageName;
        this.voice = voice;
    }

    public TtsLanguage(String engineName, String langCode, String languageName, String voice, String gender) {
        this.engineName = engineName;
        this.langCode = langCode;
        this.languageName = languageName;
        this.voice = voice;
        this.gender = gender;
    }


    public String getEngineName() {
        return engineName;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }


    @Override
    public String toString() {
        if (isVoice) {
            return this.voice + " (" + this.gender + ")";
        }

        return this.languageName;

    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getIcon() {
        return R.drawable.question_32;
    }
}
