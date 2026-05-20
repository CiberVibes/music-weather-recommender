package es.ulpgc.dacd.business.controller;

import es.ulpgc.dacd.business.model.MoodMapping;
import es.ulpgc.dacd.business.model.Track;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TrackRecommender {

    private static final Logger logger = Logger.getLogger(TrackRecommender.class.getName());

    private final TrackDatamart datamart;

    public TrackRecommender(TrackDatamart datamart) {
        this.datamart = datamart;
    }

    public void recalculateForLocation(String location, String weatherMain) {
        List<Track> tracks = recommend(weatherMain);
        datamart.saveRecommendations(location, weatherMain, tracks);
        logger.info("Recalculated " + tracks.size() + " recommendations for " + location + " (" + weatherMain + ")");
    }

    public void recalculateAll(Map<String, String> weatherByLocation) {
        weatherByLocation.forEach(this::recalculateForLocation);
    }

    public List<Track> recommend(String weatherMain) {
        Map<String, Track> seen = new LinkedHashMap<>();
        for (String tag : MoodMapping.tagsFor(weatherMain)) {
            for (Track track : datamart.findByTag(tag)) {
                seen.putIfAbsent(track.getName() + "|" + track.getArtist(), track);
            }
        }
        List<Track> result = new ArrayList<>(seen.values());
        result.sort(Comparator.comparingInt(Track::getRank));
        return result;
    }
}
