package es.ulpgc.dacd.lastfm.controller;

import es.ulpgc.dacd.lastfm.feeder.LastFmFeeder;
import es.ulpgc.dacd.lastfm.serializer.TrackSerializer;

public class Controller {

    private final LastFmFeeder feeder;
    private final TrackSerializer serializer;

    public Controller(LastFmFeeder feeder, TrackSerializer serializer) {
        this.feeder = feeder;
        this.serializer = serializer;
    }

    public void start() {
        // TODO: implementar en el Paso 5
    }
}
