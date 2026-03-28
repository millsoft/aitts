package com.millsoft.aitts;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.WebView;

import java.util.Locale;

public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        loadHelp();
    }

    public void loadHelp(){
        WebView wf = findViewById(R.id.wv);
        String language = Locale.getDefault().getLanguage();
        String helpFile = "help.html";
        if ("de".equalsIgnoreCase(language)) {
            helpFile = "help_de.html";
        }
        wf.loadUrl("file:///android_asset/help/" + helpFile);

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

}
