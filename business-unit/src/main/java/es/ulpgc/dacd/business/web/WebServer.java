package es.ulpgc.dacd.business.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.business.controller.TrackDatamart;
import es.ulpgc.dacd.business.handler.WeatherState;
import es.ulpgc.dacd.business.model.MoodMapping;
import es.ulpgc.dacd.business.model.Track;
import es.ulpgc.dacd.business.spotify.SpotifyExporter;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.List;
import java.util.Map;

public class WebServer {

    private static final int PORT = 8080;

    private final TrackDatamart datamart;
    private final WeatherState weatherState;
    private final SpotifyExporter spotify;

    public WebServer(TrackDatamart datamart, WeatherState weatherState, SpotifyExporter spotify) {
        this.datamart = datamart;
        this.weatherState = weatherState;
        this.spotify = spotify;
    }

    public void start() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");

        Javalin app = Javalin.create(config ->
                config.staticFiles.add(staticFiles -> {
                    staticFiles.directory = "/web";
                    staticFiles.location = Location.CLASSPATH;
                    staticFiles.headers = Map.of("Cache-Control", "no-cache, no-store, must-revalidate");
                }));

        app.get("/", ctx -> ctx.redirect("/index.html"));

        app.get("/api/locations", ctx -> {
            List<Map<String, String>> locations = weatherState.getAll().entrySet().stream()
                    .map(e -> Map.of(
                            "location", e.getKey(),
                            "condition", e.getValue(),
                            "mood", MoodMapping.moodName(e.getValue())))
                    .toList();
            ctx.json(locations);
        });

        app.get("/api/recommendations", ctx -> {
            String location = ctx.queryParam("location");
            if (location == null) { ctx.status(400); return; }
            List<Track> tracks = datamart.findRecommendations(location);
            List<Map<String, Object>> result = tracks.stream()
                    .map(t -> Map.<String, Object>of(
                            "name", t.getName(),
                            "artist", t.getArtist(),
                            "rank", t.getRank()))
                    .toList();
            ctx.json(result);
        });

        app.get("/auth/login", ctx -> {
            if (spotify == null) { ctx.result("Spotify not configured in .env"); return; }
            ctx.redirect(spotify.getAuthUrl());
        });

        app.get("/callback", ctx -> {
            String code = ctx.queryParam("code");
            if (code != null && spotify != null) {
                try {
                    spotify.exchangeCode(code);
                } catch (Exception e) {
                    System.err.println("OAuth error: " + e.getMessage());
                }
            }
            ctx.redirect("/");
        });

        app.get("/api/token", ctx -> {
            if (spotify == null || !spotify.isAuthorized()) {
                ctx.status(401).result("Not authorized");
                return;
            }
            try {
                ctx.json(Map.of("access_token", spotify.getAccessToken()));
            } catch (Exception e) {
                ctx.status(500).result("Token refresh failed");
            }
        });

        app.get("/api/search", ctx -> {
            String name = ctx.queryParam("name");
            String artist = ctx.queryParam("artist");
            if (name == null || artist == null || spotify == null || !spotify.isAuthorized()) {
                ctx.status(400); return;
            }
            try {
                String uri = spotify.searchTrack(name, artist);
                if (uri == null) { ctx.status(404).result("Not found"); return; }
                ctx.json(Map.of("uri", uri));
            } catch (Exception e) {
                ctx.status(500).result(e.getMessage());
            }
        });

        app.post("/api/play", ctx -> {
            if (spotify == null || !spotify.isAuthorized()) { ctx.status(401); return; }
            try {
                JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();
                String did = body.get("deviceId").getAsString();
                String uri = body.get("uri").getAsString();
                int status = spotify.playTrack(did, uri);
                if (status == 204) ctx.status(200).result("ok");
                else ctx.status(status).result("Play failed: " + status);
            } catch (Exception e) {
                ctx.status(500).result(e.getMessage());
            }
        });

        app.start(PORT);
        System.out.println("\nWeb UI available at http://localhost:" + PORT);
        System.out.println("Open your browser and go to http://localhost:" + PORT);
    }
}
