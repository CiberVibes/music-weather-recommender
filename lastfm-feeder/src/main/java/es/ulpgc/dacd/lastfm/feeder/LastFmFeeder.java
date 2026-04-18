package es.ulpgc.dacd.lastfm.feeder;

import es.ulpgc.dacd.lastfm.model.Track;

import java.util.List;

public interface LastFmFeeder {
    List<Track> feed();
}
