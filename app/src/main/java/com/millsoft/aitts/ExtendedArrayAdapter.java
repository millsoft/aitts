package com.millsoft.aitts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ExtendedArrayAdapter extends ArrayAdapter<TtsLanguage> {

    public interface OnPreviewClickListener {
        void onPreviewClick(TtsLanguage language);
    }

    private int selectedPosition;
    private final OnPreviewClickListener onPreviewClickListener;

    public ExtendedArrayAdapter(@NonNull Context context, @NonNull List<TtsLanguage> objects, int selectedPosition, @Nullable OnPreviewClickListener onPreviewClickListener) {
        super(context, R.layout.voice_selector, objects);
        this.selectedPosition = selectedPosition;
        this.onPreviewClickListener = onPreviewClickListener;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final TtsLanguage ttsLanguage = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.voice_selector, parent, false);
        }

        RadioButton radioButton = convertView.findViewById(R.id.voice_selector_radio);
        TextView textView = convertView.findViewById(R.id.voice_selector_list_label);
        ImageButton previewButton = convertView.findViewById(R.id.voice_selector_preview_btn);

        if (ttsLanguage != null) {
            textView.setText(ttsLanguage.toString());
        } else {
            textView.setText("");
        }

        radioButton.setChecked(position == selectedPosition);

        View.OnClickListener selectVoiceClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedPosition(position);
            }
        };

        convertView.setOnClickListener(selectVoiceClick);
        radioButton.setOnClickListener(selectVoiceClick);
        textView.setOnClickListener(selectVoiceClick);

        previewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onPreviewClickListener != null && ttsLanguage != null) {
                    onPreviewClickListener.onPreviewClick(ttsLanguage);
                }
            }
        });

        return convertView;

    }
}
