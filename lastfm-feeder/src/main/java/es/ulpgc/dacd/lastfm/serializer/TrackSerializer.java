package es.ulpgc.dacd.lastfm.serializer;

import es.ulpgc.dacd.lastfm.model.Track;

public interface TrackSerializer {
    void serialize(Track track);
}
