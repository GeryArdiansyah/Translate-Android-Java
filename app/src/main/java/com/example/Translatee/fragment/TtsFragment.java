package com.example.Translatee.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.Translatee.R;

import java.util.Locale;

public class TtsFragment extends Fragment implements OnInitListener {

    private TextToSpeech textToSpeech;
    private EditText editText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tts, container, false);

        editText = rootView.findViewById(R.id.editText);
        ImageView buttonSpeak = rootView.findViewById(R.id.buttonSpeak); // Mengganti tipe menjadi ImageView
        buttonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        // Periksa apakah TextToSpeech sudah terinstal
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, 1);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // TextToSpeech sudah terinstal, inisialisasi
                textToSpeech = new TextToSpeech(requireContext(), this);
            } else {
                // TextToSpeech belum terinstal, tampilkan pesan ke pengguna
                Toast.makeText(requireContext(), "Text-to-Speech belum terinstal, silakan instal terlebih dahulu.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set the language to Indonesian (ID)
            int result = textToSpeech.setLanguage(new Locale("id", "ID"));

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                editText.setEnabled(false);
                speakOut("Bahasa Indonesia tidak didukung pada perangkat ini.");
            } else {
                editText.setEnabled(true);
            }
        } else {
            editText.setEnabled(false);
            speakOut("Text-to-Speech tidak tersedia pada perangkat ini.");
        }
    }

    public void speak() {
        String teksTerucap = editText.getText().toString().trim();
        if (!teksTerucap.isEmpty()) {
            speakOut(teksTerucap);
        } else {
            Toast.makeText(requireContext(), "Teks tidak boleh kosong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void speakOut(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
