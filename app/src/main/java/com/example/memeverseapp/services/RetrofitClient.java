package com.example.memeverseapp.services;

import android.content.Context;
import android.util.Log;

import com.example.memeverseapp.utils.PreferencesManager;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = "RetrofitClient";

    private static Retrofit retrofit = null;
    private static String currentBaseUrl = null;
    private static PreferencesManager prefManager = null;

    private static final CookieManager cookieManager = new CookieManager();

    static {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    public static void init(Context context) {
        prefManager = new PreferencesManager(context);
        currentBaseUrl = prefManager.getBaseUrl();

        if (currentBaseUrl == null || currentBaseUrl.trim().isEmpty()) {
            currentBaseUrl = "http://10.0.2.2/memeverse/";
        }

        Log.d(TAG, "Initialized with base URL: " + currentBaseUrl);
    }

    public static void updateBaseUrl(Context context, String newBaseUrl) {
        if (prefManager == null) {
            prefManager = new PreferencesManager(context);
        }

        prefManager.setBaseUrl(newBaseUrl);
        currentBaseUrl = newBaseUrl;
        retrofit = null;

        Log.d(TAG, "Base URL updated to: " + currentBaseUrl);
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            if (prefManager != null) {
                currentBaseUrl = prefManager.getBaseUrl();
            }

            if (currentBaseUrl == null || currentBaseUrl.trim().isEmpty()) {
                currentBaseUrl = "http://10.0.2.2/memeverse/";
            }

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(currentBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            Log.d(TAG, "Retrofit created with URL: " + currentBaseUrl);
        }

        return retrofit;
    }

    public static String getFullUrl(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }

        if (currentBaseUrl == null || currentBaseUrl.trim().isEmpty()) {
            currentBaseUrl = "http://10.0.2.2/memeverse/";
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return currentBaseUrl + path;
    }

    public static void clearCookies() {
        cookieManager.getCookieStore().removeAll();
    }
}