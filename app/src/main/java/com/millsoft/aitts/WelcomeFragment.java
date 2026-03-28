package com.millsoft.aitts;

import static com.millsoft.aitts.global.Constants.API_KEY_GOOGLE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class WelcomeFragment extends Fragment implements View.OnClickListener {
    public WelcomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_welcome, container, false);

        TextView btnWelcomeNo = v.findViewById(R.id.btn_welcome_no);
        btnWelcomeNo.setOnClickListener(this);

        Button btnWelcomeYes = v.findViewById(R.id.btn_welcome_yes);
        btnWelcomeYes.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {


            case R.id.btn_welcome_no:
                Intent i = new Intent(getContext(), HelpActivity.class);
                startActivity(i);
                break;

            case R.id.btn_welcome_yes:
                showApiKeyDialog();
                break;

        }
    }


    public void showApiKeyDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.google_tts_api_key);

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        builder.setView(input);

        String val = Settings.getSetting(API_KEY_GOOGLE);

        input.setText(val);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Settings.setSetting(API_KEY_GOOGLE, input.getText().toString());
                ((TtsSettingsActivity) getActivity()).startAppFromWelcome();
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
                        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });
        input.requestFocus();


        builder.show();
    }
}
