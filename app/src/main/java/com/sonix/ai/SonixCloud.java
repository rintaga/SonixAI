package com.sonix.ai;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Small dependency-free Supabase REST/Auth client for Sonix AI. */
public final class SonixCloud {
    private SonixCloud() { }

    public static JSONObject signUp(String baseUrl, String publishableKey, String email, String password) throws Exception {
        JSONObject body = new JSONObject();
        body.put("email", email);
        body.put("password", password);
        return requestObject(trimBase(baseUrl) + "/auth/v1/signup", "POST", body,
                publishableKey, null, null);
    }

    public static JSONObject signIn(String baseUrl, String publishableKey, String email, String password) throws Exception {
        JSONObject body = new JSONObject();
        body.put("email", email);
        body.put("password", password);
        return requestObject(trimBase(baseUrl) + "/auth/v1/token?grant_type=password", "POST", body,
                publishableKey, null, null);
    }

    public static JSONObject refreshSession(String baseUrl, String publishableKey, String refreshToken) throws Exception {
        JSONObject body = new JSONObject();
        body.put("refresh_token", refreshToken);
        return requestObject(trimBase(baseUrl) + "/auth/v1/token?grant_type=refresh_token", "POST", body,
                publishableKey, null, null);
    }

    public static String googleOAuthUrl(String baseUrl, String publishableKey) {
        return Uri.parse(trimBase(baseUrl) + "/auth/v1/authorize")
                .buildUpon()
                .appendQueryParameter("provider", "google")
                .appendQueryParameter("redirect_to", "sonix://auth/callback")
                .appendQueryParameter("apikey", publishableKey)
                .build().toString();
    }

    public static JSONObject invokeMemory(String baseUrl, String publishableKey, String accessToken,
                                          JSONObject payload) throws Exception {
        return requestObject(trimBase(baseUrl) + "/functions/v1/memory-assist", "POST", payload,
                publishableKey, accessToken, null);
    }

    public static void saveMessage(String baseUrl, String publishableKey, String accessToken,
                                   String conversationId, String title, String role, String content) throws Exception {
        JSONObject conversation = new JSONObject();
        conversation.put("id", conversationId);
        conversation.put("title", safeTitle(title));
        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        timestamp.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        conversation.put("updated_at", timestamp.format(new Date()));
        requestObject(trimBase(baseUrl) + "/rest/v1/sonix_conversations?on_conflict=id", "POST",
                conversation, publishableKey, accessToken, "resolution=merge-duplicates,return=minimal");

        JSONObject message = new JSONObject();
        message.put("conversation_id", conversationId);
        message.put("role", role);
        message.put("content", content);
        requestObject(trimBase(baseUrl) + "/rest/v1/sonix_messages", "POST", message,
                publishableKey, accessToken, "return=minimal");
    }

    public static JSONArray listConversations(String baseUrl, String publishableKey, String accessToken) throws Exception {
        String url = trimBase(baseUrl) + "/rest/v1/sonix_conversations?select=id,title,updated_at&order=updated_at.desc&limit=40";
        return requestArray(url, "GET", null, publishableKey, accessToken, null);
    }

    public static JSONArray listMessages(String baseUrl, String publishableKey, String accessToken,
                                         String conversationId) throws Exception {
        String safeId = Uri.encode(conversationId);
        String url = trimBase(baseUrl) + "/rest/v1/sonix_messages?select=role,content,created_at&conversation_id=eq." + safeId
                + "&order=created_at.asc&limit=150";
        return requestArray(url, "GET", null, publishableKey, accessToken, null);
    }

    public static JSONArray listMemories(String baseUrl, String publishableKey, String accessToken) throws Exception {
        String url = trimBase(baseUrl) + "/rest/v1/sonix_memories?select=id,content,category,created_at&order=updated_at.desc&limit=60";
        return requestArray(url, "GET", null, publishableKey, accessToken, null);
    }

    public static void deleteMemory(String baseUrl, String publishableKey, String accessToken, String memoryId) throws Exception {
        String url = trimBase(baseUrl) + "/rest/v1/sonix_memories?id=eq." + Uri.encode(memoryId);
        requestObject(url, "DELETE", null, publishableKey, accessToken, "return=minimal");
    }

    private static JSONObject requestObject(String url, String method, JSONObject body,
                                            String publishableKey, String accessToken, String prefer) throws Exception {
        String response = requestText(url, method, body, publishableKey, accessToken, prefer);
        if (response == null || response.trim().isEmpty()) return new JSONObject();
        try { return new JSONObject(response); }
        catch (Exception e) { throw new IllegalStateException("Respons Supabase bukan JSON object: " + response); }
    }

    private static JSONArray requestArray(String url, String method, JSONObject body,
                                          String publishableKey, String accessToken, String prefer) throws Exception {
        String response = requestText(url, method, body, publishableKey, accessToken, prefer);
        if (response == null || response.trim().isEmpty()) return new JSONArray();
        try { return new JSONArray(response); }
        catch (Exception e) { throw new IllegalStateException("Respons Supabase bukan JSON array: " + response); }
    }

    private static String requestText(String url, String method, JSONObject body,
                                      String publishableKey, String accessToken, String prefer) throws Exception {
        if (publishableKey == null || publishableKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Supabase publishable/anon key belum diisi.");
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(45000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("apikey", publishableKey.trim());
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + accessToken.trim());
        }
        if (prefer != null && !prefer.isEmpty()) connection.setRequestProperty("Prefer", prefer);
        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream output = connection.getOutputStream()) { output.write(bytes); }
        }
        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
        String response = readAll(stream);
        connection.disconnect();
        if (status < 200 || status >= 300) {
            String message = response;
            try {
                JSONObject error = new JSONObject(response);
                message = error.optString("msg", error.optString("message", error.optString("error_description", response)));
            } catch (Exception ignored) { }
            throw new IllegalStateException("Supabase HTTP " + status + ": " + message);
        }
        return response;
    }

    private static String readAll(InputStream input) throws Exception {
        if (input == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }

    private static String trimBase(String value) {
        if (value == null) return "";
        String result = value.trim();
        while (result.endsWith("/")) result = result.substring(0, result.length() - 1);
        return result;
    }

    private static String safeTitle(String value) {
        if (value == null || value.trim().isEmpty()) return "Obrolan Sonix";
        String title = value.trim().replace('\n', ' ');
        return title.length() > 90 ? title.substring(0, 90) : title;
    }
}
