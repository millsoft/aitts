package com.millsoft.aitts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.millsoft.aitts.engines.GoogleTtsEngine;
import com.millsoft.aitts.engines.TtsEngine;
import com.millsoft.aitts.global.Constants;
import com.millsoft.aitts.global.Window;

import java.util.concurrent.atomic.AtomicInteger;

public class TtsSettingsActivity extends AppCompatActivity implements StartupLoadingFragment.Listener {

    private static final String TAG = "TTS-Settings";

    private TextToSpeech tts;

    public TtsEngine ttsEngine;
    private final AtomicInteger startupRequestVersion = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tts_settings);
        Settings.setContext(getApplicationContext());

        startApp();

    }


    /**
     * Start the app
     * Show either the welcome page (if no api key was entered)
     * ... or the voice selector
     */
    public void startApp() {
        String apiKey = Settings.getSetting(Constants.API_KEY_GOOGLE);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            showWelcome();
            return;
        }
        showStartupLoading();
        loadVoicesOnStartup();
    }

    private void openFragment(final Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.isStateSaved()) {
            Log.w(TAG, "Skipping fragment transaction because state is already saved");
            return;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private void replaceRootFragment(final Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.isStateSaved()) {
            Log.w(TAG, "Skipping root fragment transaction because state is already saved");
            return;
        }
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        openFragment(fragment, false);
    }

    private void showWelcome() {
        replaceRootFragment(new WelcomeFragment());
    }

    private void showStartupLoading() {
        replaceRootFragment(new StartupLoadingFragment());
    }

    private void showSettings() {
        replaceRootFragment(new SettingsFragment());
    }

    private StartupLoadingFragment getStartupLoadingFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof StartupLoadingFragment) {
            return (StartupLoadingFragment) fragment;
        }
        return null;
    }

    private void setStartupLoadingState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StartupLoadingFragment fragment = getStartupLoadingFragment();
                if (fragment != null) {
                    fragment.renderLoading();
                }
            }
        });
    }

    private void setStartupErrorState(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                StartupLoadingFragment fragment = getStartupLoadingFragment();
                if (fragment != null) {
                    fragment.renderError(message);
                } else {
                    showStartupLoading();
                    StartupLoadingFragment refreshedFragment = getStartupLoadingFragment();
                    if (refreshedFragment != null) {
                        refreshedFragment.renderError(message);
                    }
                }
            }
        });
    }

    private void loadVoicesOnStartup() {
        final int requestId = startupRequestVersion.incrementAndGet();
        setStartupLoadingState();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "starting worker loop");
                TtsEngine loadedEngine = new TtsEngine(getApplicationContext(), false);
                final int voicesCount = loadedEngine.getAllEngineVoicesCount();
                if (requestId != startupRequestVersion.get()) {
                    return;
                }

                if (voicesCount > 0) {
                    ttsEngine = loadedEngine;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }
                            if (requestId != startupRequestVersion.get()) {
                                return;
                            }
                            showSettings();
                        }
                    });
                } else {
                    setStartupErrorState(getString(R.string.invalid_api_key));
                }

                Log.i(TAG, "worker loop done");
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Hide welcome page and show the settings page
     */
    public void startAppFromWelcome() {
        showStartupLoading();
        loadVoicesOnStartup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.mnu_google_key:
                showGoogleApiKeyDialog();
                return true;

            case R.id.mnu_help:
                Intent i = new Intent(this, HelpActivity.class);
                startActivity(i);
                return true;

            case R.id.mnu_about:
                showAbout();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    public void showGoogleApiKeyDialog() {
        showGoogleApiKeyDialog(null);
    }

    public void showGoogleApiKeyDialog(final Runnable onSaved) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.google_tts_api_key);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        builder.setView(input);

        String settingKey = Constants.API_KEY_GOOGLE;

        String val = Settings.getSetting(settingKey);

        input.setText(val);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Context ctx = TtsSettingsActivity.this;

                Settings.setSetting(settingKey, input.getText().toString());
                if (onSaved != null) {
                    onSaved.run();
                    return;
                }
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        GoogleTtsEngine googleTtsEngine = new GoogleTtsEngine();
                        googleTtsEngine.setContext(ctx);

                        if (!googleTtsEngine.isApiKeyValid()) {
                            Window w = new Window(ctx);
                            w.MsgBox(R.string.invalid_api_key);
                        }

                        Looper.loop();

                    }
                };

                Thread thread = new Thread(runnable);
                thread.start();


            }
        });
        builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                input.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) TtsSettingsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });
        input.requestFocus();


        builder.show();

    }

    public void onStartupRetry() {
        loadVoicesOnStartup();
    }

    public void onStartupEditApiKey() {
        showGoogleApiKeyDialog(new Runnable() {
            @Override
            public void run() {
                loadVoicesOnStartup();
            }
        });
    }

    public void showApiKeyDialog() {
        showGoogleApiKeyDialog();
    }


    private void showAbout() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);


        String version = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        dlgAlert.setMessage(getText(R.string.app_name) + "\nVersion " + version + "\nMichael Milawski\nMillsoft");

        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }


    public void btnTest(View view) {

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.speak(Constants.TTS_TEST_SAMPLE_TEXT, TextToSpeech.QUEUE_FLUSH, null, "tts_test");
            }
        });


    }

}
