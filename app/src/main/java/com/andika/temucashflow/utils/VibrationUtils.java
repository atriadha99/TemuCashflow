package com.andika.temucashflow.utils;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import com.andika.temucashflow.data.SharedPrefManager;

public class VibrationUtils {

    public static void vibrate(Context context, long duration) {
        if (SharedPrefManager.getInstance(context).isVibrationEnabled()) {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }
    }
}
