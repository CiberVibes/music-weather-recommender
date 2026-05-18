package es.ulpgc.dacd.business.recommendation;

import es.ulpgc.dacd.business.datamart.TrackDatamart;
import es.ulpgc.dacd.business.model.Track;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TrackRecommender {

    private final TrackDatamart datamart;

    public TrackRecommender(TrackDatamart datamart) {
        this.datamart = datamart;
    }

    public List<Track> recommend(String weatherMain) {
        Map<String, Track> seen = new LinkedHashMap<>();
        for (String tag : MoodMapper.tagsFor(weatherMain)) {
            for (Track track : datamart.findByTag(tag)) {
                seen.putIfAbsent(track.getName() + "|" + track.getArtist(), track);
            }
        }
        List<Track> result = new ArrayList<>(seen.values());
        result.sort(Comparator.comparingInt(Track::getRank));
        return result;
    }
}
