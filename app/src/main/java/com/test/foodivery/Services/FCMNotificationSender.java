package com.test.foodivery.Services;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FCMNotificationSender {

    private static final String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Replace with your server key from Firebase Console
    private static final String SERVER_KEY = "bf24a211ee5e64553a4081fbf9fdd3babeeba9fa";

    public interface NotificationCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public static void sendNotification(String token, String title, String message,
                                        Context context, NotificationCallback callback) {

        OkHttpClient client = new OkHttpClient();

        // Create JSON payload
        FCMessage fcMessage = new FCMessage(token, new NotificationData(title, message));
        String json = new Gson().toJson(fcMessage);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(FCM_API_URL)
                .post(body)
                .addHeader("Authorization", "key=" + SERVER_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FCM", "Failed to send notification: " + e.getMessage());
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("FCM", "Notification sent successfully");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    String error = response.body().string();
                    Log.e("FCM", "Failed to send notification: " + error);
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                }
                response.close();
            }
        });
    }

    // Helper classes for JSON serialization
    private static class FCMessage {
        public String to;
        public NotificationData notification;

        public FCMessage(String to, NotificationData notification) {
            this.to = to;
            this.notification = notification;
        }
    }

    private static class NotificationData {
        public String title;
        public String body;

        public NotificationData(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }
}