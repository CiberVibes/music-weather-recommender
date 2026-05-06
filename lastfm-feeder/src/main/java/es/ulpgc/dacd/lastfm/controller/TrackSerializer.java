package es.ulpgc.dacd.lastfm.controller;

import es.ulpgc.dacd.lastfm.model.Track;

public interface TrackSerializer {
    void serialize(Track track);
}
