package com.andika.temucashflow.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ImageUtils {

    public static String saveBitmap(Context context, Bitmap bitmap) {
        File directory = new File(context.getFilesDir(), "receipts");
        if (!directory.exists() && !directory.mkdirs()) {
            return null;
        }

        String fileName = UUID.randomUUID().toString() + ".jpg";
        File file = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("ImageUtils", "Error saving bitmap", e);
            return null;
        }
    }
}
