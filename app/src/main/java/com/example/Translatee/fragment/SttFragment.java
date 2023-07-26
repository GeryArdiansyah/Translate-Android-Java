package com.example.Translatee.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.Translatee.R;

import java.util.ArrayList;
import java.util.Locale;

public class SttFragment extends Fragment {

    private SpeechRecognizer speechRecognizer;
    private TextView resultTextView;
    private boolean isListening = false;
    private boolean isCooldown = false;

    public SttFragment() {
        // Konstruktor publik kosong diperlukan
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stt, container, false);
        resultTextView = rootView.findViewById(R.id.textView);
        ImageView speakButton = rootView.findViewById(R.id.speakButton);
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isListening && !isCooldown) {
                    startSpeechToText();
                    resultTextView.setText("Merekam...");
                    isCooldown = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isCooldown = false;
                        }
                    }, 2000); // Atur waktu cooldown (dalam milidetik), sesuaikan kebutuhan
                } else if (isListening) {
                    stopSpeechToText();
                    resultTextView.setText("Katakan sesuatu...");
                }
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());

        return rootView;
    }

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private void startSpeechToText() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            startSpeechRecognition();
        }
    }

    private void startSpeechRecognition() {
        Locale bahasaIndonesia = new Locale("id", "ID");
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    resultTextView.setText("Tidak ada suara yang terdeteksi.");
                } else if (error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT) {
                    resultTextView.setText("Waktu koneksi habis. Coba lagi.");
                    // Coba kembali pengenalan ucapan setelah jeda singkat (misalnya, 1 detik)
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startSpeechRecognition();
                        }
                    }, 1000); // Sesuaikan jeda sesuai kebutuhan
                } else if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    resultTextView.setText("Izin mikrofon diperlukan untuk pengenalan ucapan.");
                    // Meminta izin penggunaan mikrofon
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            PERMISSIONS_REQUEST_RECORD_AUDIO);
                } else {
                    resultTextView.setText("Error: " + error);
                }
                isListening = false;
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> hasilUcapan = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (hasilUcapan != null && !hasilUcapan.isEmpty()) {
                    String teksTerucap = hasilUcapan.get(0);
                    resultTextView.setText(teksTerucap);
                } else {
                    resultTextView.setText("Tidak ada ucapan yang terdeteksi.");
                }
                isListening = false;
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, bahasaIndonesia);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Mulai Berbicara");
        speechRecognizer.startListening(intent);

        isListening = true;
    }

    private void stopSpeechToText() {
        speechRecognizer.stopListening();
        isListening = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognition();
            } else {
                Toast.makeText(requireContext(), "Izin mikrofon diperlukan untuk pengenalan ucapan.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
