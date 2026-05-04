package es.ulpgc.dacd.lastfm.publisher;

import com.google.gson.Gson;
import es.ulpgc.dacd.lastfm.model.Track;
import es.ulpgc.dacd.lastfm.model.TrackEvent;

public class GsonTrackEventSerializer implements TrackEventSerializer {

    private final String ss;
    private final Gson gson;

    public GsonTrackEventSerializer(String ss) {
        this.ss = ss;
        this.gson = new Gson();
    }

    @Override
    public String serialize(Track track) {
        return gson.toJson(new TrackEvent(ss, track));
    }
}
