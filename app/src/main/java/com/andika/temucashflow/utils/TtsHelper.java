package com.andika.temucashflow.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.andika.temucashflow.data.SharedPrefManager;

import java.util.Locale;

public class TtsHelper {
    private static final String TAG = "TtsHelper";
    private TextToSpeech tts;
    private boolean isReady = false;

    public TtsHelper(Context context) {
        if (!SharedPrefManager.getInstance(context).isTtsEnabled()) return;

        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("id", "ID"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported");
                } else {
                    isReady = true;
                }
            } else {
                Log.e(TAG, "Initialization failed");
            }
        });
    }

    public void speak(String text) {
        if (isReady && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
