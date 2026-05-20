package es.ulpgc.dacd.lastfm.controller;

import es.ulpgc.dacd.lastfm.model.Track;

import java.util.List;

public interface LastFmFeeder {
    List<Track> feed();
}
