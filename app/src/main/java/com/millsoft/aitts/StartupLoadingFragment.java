package com.millsoft.aitts;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class StartupLoadingFragment extends Fragment {
    public interface Listener {
        void onStartupRetry();

        void onStartupEditApiKey();
    }

    private Listener listener;

    private ProgressBar progressBar;
    private TextView txtStatus;
    private TextView txtError;
    private Button btnRetry;
    private Button btnEditKey;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_startup_loading, container, false);
        progressBar = view.findViewById(R.id.startup_progress_bar);
        txtStatus = view.findViewById(R.id.startup_status);
        txtError = view.findViewById(R.id.startup_error);
        btnRetry = view.findViewById(R.id.startup_btn_retry);
        btnEditKey = view.findViewById(R.id.startup_btn_edit_key);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onStartupRetry();
                }
            }
        });
        btnEditKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onStartupEditApiKey();
                }
            }
        });

        renderLoading();
        return view;
    }

    public void renderLoading() {
        if (!isAdded() || getView() == null) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        txtStatus.setText(R.string.startup_loading_title);
        txtError.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        btnEditKey.setVisibility(View.GONE);
    }

    public void renderError(String message) {
        if (!isAdded() || getView() == null) {
            return;
        }
        progressBar.setVisibility(View.GONE);
        txtStatus.setText(R.string.startup_error_title);
        txtError.setText(message);
        txtError.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
        btnEditKey.setVisibility(View.VISIBLE);
    }
}
