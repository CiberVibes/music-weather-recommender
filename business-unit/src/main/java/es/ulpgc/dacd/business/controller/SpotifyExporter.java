package es.ulpgc.dacd.business.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class SpotifyExporter {

    private static final String AUTH_URL = "https://accounts.spotify.com/authorize";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String API_URL = "https://api.spotify.com/v1";
    private static final String REDIRECT_URI = "http://127.0.0.1:8080/callback";
    private static final String SCOPE = String.join(" ",
            "streaming",
            "user-read-email",
            "user-read-private",
            "user-modify-playback-state");

    private final String clientId;
    private final String clientSecret;
    private final HttpClient http;

    private String accessToken;
    private String refreshToken;
    private Instant tokenExpiry;

    public SpotifyExporter(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.http = HttpClient.newHttpClient();
    }

    public String getAuthUrl() {
        return AUTH_URL
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8);
    }

    public void exchangeCode(String code) throws Exception {
        String body = "grant_type=authorization_code"
                + "&code=" + code
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);
        JsonObject json = postToTokenEndpoint(body);
        storeTokens(json);
    }

    public boolean isAuthorized() {
        return accessToken != null;
    }

    public String searchTrack(String name, String artist) throws Exception {
        String q = URLEncoder.encode(name + " " + artist, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/search?q=" + q + "&type=track&limit=1"))
                .header("Authorization", "Bearer " + getAccessToken())
                .GET().build();
        HttpResponse<String> response = sendWithRateLimitRetry(request);
        return extractTrackUri(response);
    }

    public int playTrack(String deviceId, String uri) throws Exception {
        transferPlayback(deviceId);
        Thread.sleep(300);
        return sendPlayRequest(deviceId, uri);
    }

    private HttpResponse<String> sendWithRateLimitRetry(HttpRequest request) throws Exception {
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 429) return response;
        long retryAfter = response.headers().firstValueAsLong("Retry-After").orElse(5L);
        Thread.sleep(retryAfter * 1000L);
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String extractTrackUri(HttpResponse<String> response) throws Exception {
        String body = response.body();
        JsonObject root;
        try {
            root = JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception e) {
            throw new Exception("Spotify HTTP " + response.statusCode() + ": " + body.substring(0, Math.min(300, body.length())));
        }
        if (root.has("error") || !root.has("tracks")) return null;
        JsonArray items = root.getAsJsonObject("tracks").getAsJsonArray("items");
        if (items == null || items.isEmpty()) return null;
        return items.get(0).getAsJsonObject().get("uri").getAsString();
    }

    private void transferPlayback(String deviceId) throws Exception {
        JsonArray deviceIds = new JsonArray();
        deviceIds.add(deviceId);
        JsonObject payload = new JsonObject();
        payload.add("device_ids", deviceIds);
        payload.addProperty("play", false);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/me/player"))
                .header("Authorization", "Bearer " + getAccessToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();
        http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private int sendPlayRequest(String deviceId, String uri) throws Exception {
        JsonArray uris = new JsonArray();
        uris.add(uri);
        JsonObject payload = new JsonObject();
        payload.add("uris", uris);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/me/player/play?device_id=" + deviceId))
                .header("Authorization", "Bearer " + getAccessToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
    }

    public String getAccessToken() throws Exception {
        if (isExpired()) refreshAccessToken();
        return accessToken;
    }

    private void refreshAccessToken() throws Exception {
        String body = "grant_type=refresh_token&refresh_token=" + refreshToken;
        JsonObject json = postToTokenEndpoint(body);
        storeTokens(json);
    }

    private boolean isExpired() {
        return tokenExpiry != null && Instant.now().isAfter(tokenExpiry.minusSeconds(60));
    }

    private void storeTokens(JsonObject json) {
        accessToken = json.get("access_token").getAsString();
        if (json.has("refresh_token") && !json.get("refresh_token").isJsonNull()) {
            refreshToken = json.get("refresh_token").getAsString();
        }
        int expiresIn = json.has("expires_in") ? json.get("expires_in").getAsInt() : 3600;
        tokenExpiry = Instant.now().plusSeconds(expiresIn);
    }

    private JsonObject postToTokenEndpoint(String body) throws Exception {
        String credentials = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        String responseBody = http.send(request, HttpResponse.BodyHandlers.ofString()).body();
        return JsonParser.parseString(responseBody).getAsJsonObject();
    }
}
