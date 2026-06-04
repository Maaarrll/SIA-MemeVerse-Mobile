package com.example.memeverseapp.network;

import android.content.Context;
import android.util.Log;

import com.example.memeverseapp.utils.PreferencesManager;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
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

    public static void init(Context context) {
        prefManager = new PreferencesManager(context);
        currentBaseUrl = prefManager.getBaseUrl();
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
            if (prefManager == null) {
                throw new IllegalStateException("RetrofitClient.init() must be called before getClient()");
            }

            currentBaseUrl = prefManager.getBaseUrl();

            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            JavaNetCookieJar cookieJar = new JavaNetCookieJar(cookieManager);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .cookieJar(cookieJar)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(currentBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            Log.d(TAG, "Retrofit client created with URL: " + currentBaseUrl);
        }

        return retrofit;
    }

    public static String getFullUrl(String path) {
        if (path == null || path.isEmpty()) return null;

        if (path.startsWith("http")) {
            if (path.contains("loki-store.local")) {
                try {
                    URL oldUrl = new URL(path);
                    URL baseUrl = new URL(currentBaseUrl);

                    String newPath = path.replace(oldUrl.getHost(), baseUrl.getHost());

                    if (oldUrl.getPort() != -1 && baseUrl.getPort() != -1) {
                        newPath = newPath.replace(":" + oldUrl.getPort(), ":" + baseUrl.getPort());
                    }

                    return newPath;

                } catch (Exception e) {
                    return path;
                }
            } else {
                return path;
            }
        }

        if (!path.contains("/")) {
            path = "avatars/" + path;
        }

        return currentBaseUrl + path;
    }
}