package com.example.memeverseapp.utils;

import android.app.Activity;
import android.widget.Toast;

public class ToastUtils {

    public static void showSuccess(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    public static void showError(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    public static void showInfo(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
}