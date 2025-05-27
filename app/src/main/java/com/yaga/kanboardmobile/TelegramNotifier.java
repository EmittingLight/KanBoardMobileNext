package com.yaga.kanboardmobile;

import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TelegramNotifier {

    private static final String TAG = "TelegramNotifier";
    private static final String CHAT_ID = "425774397"; // 👈 Твой Telegram chat_id
    private static final String BOT_TOKEN = BuildConfig.TELEGRAM_BOT_TOKEN;

    public static void send(String message) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Log.d(TAG, "BOT_TOKEN: " + BOT_TOKEN);
            Log.d(TAG, "Sending message to Telegram: " + message);

            String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
            String data = "chat_id=" + CHAT_ID + "&text=" + URLEncoder.encode(message, "UTF-8");

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Telegram response code: " + responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Log.d(TAG, "Telegram response body: " + response.toString());

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при отправке в Telegram", e);
        }
    }
}

