package es.ulpgc.dacd.lastfm.controller;

import es.ulpgc.dacd.lastfm.model.Track;

public interface TrackEventSerializer {
    String serialize(Track track);
}
