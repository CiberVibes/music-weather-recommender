package es.ulpgc.dacd.business.recommendation;

import es.ulpgc.dacd.business.datamart.TrackDatamart;
import es.ulpgc.dacd.business.model.Track;

import java.util.List;

public class TrackRecommender {

    private final TrackDatamart datamart;

    public TrackRecommender(TrackDatamart datamart) {
        this.datamart = datamart;
    }

    public List<Track> recommend(String weatherMain) {
        for (String tag : MoodMapper.tagsFor(weatherMain)) {
            List<Track> tracks = datamart.findByTag(tag);
            if (!tracks.isEmpty()) return tracks;
        }
        return List.of();
    }
}
