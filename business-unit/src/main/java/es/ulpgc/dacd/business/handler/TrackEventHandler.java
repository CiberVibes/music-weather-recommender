package es.ulpgc.dacd.business.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.business.controller.TrackDatamart;
import es.ulpgc.dacd.business.controller.TrackRecommender;
import es.ulpgc.dacd.business.model.Tag;
import es.ulpgc.dacd.business.model.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TrackEventHandler implements EventHandler {

    private static final Logger logger = Logger.getLogger(TrackEventHandler.class.getName());

    private final TrackDatamart datamart;
    private final TrackRecommender recommender;
    private final WeatherState weatherState;
    private volatile boolean active = false;

    public TrackEventHandler(TrackDatamart datamart, TrackRecommender recommender, WeatherState weatherState) {
        this.datamart = datamart;
        this.recommender = recommender;
        this.weatherState = weatherState;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void handle(String json) {
        try {
            Track track = parse(json);
            datamart.save(track);
            if (active && weatherState.hasData()) {
                recommender.recalculateAll(weatherState.getAll());
            }
        } catch (Exception e) {
            logger.severe("Failed to handle track event: " + e.getMessage());
        }
    }

    private Track parse(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        String name = obj.get("name").getAsString();
        String artist = obj.get("artist").getAsString();
        String mbid = getString(obj, "mbid");
        String url = getString(obj, "url");
        int rank = obj.get("rank").getAsInt();
        String ts = obj.get("ts").getAsString();
        String ss = obj.get("ss").getAsString();
        List<Tag> tags = parseTags(obj.getAsJsonArray("tags"));
        return new Track(name, artist, mbid, url, rank, ts, ss, tags);
    }

    private String getString(JsonObject obj, String key) {
        var element = obj.get(key);
        return (element != null && !element.isJsonNull()) ? element.getAsString() : null;
    }

    private List<Tag> parseTags(JsonArray tagsArray) {
        List<Tag> tags = new ArrayList<>();
        if (tagsArray == null) return tags;
        for (var element : tagsArray) {
            JsonObject tagObj = element.getAsJsonObject();
            tags.add(new Tag(tagObj.get("name").getAsString(), tagObj.get("count").getAsInt()));
        }
        return tags;
    }
}
