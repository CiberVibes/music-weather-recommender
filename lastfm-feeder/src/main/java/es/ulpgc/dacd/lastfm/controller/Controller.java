package es.ulpgc.dacd.lastfm.controller;

import es.ulpgc.dacd.lastfm.feeder.LastFmFeeder;
import es.ulpgc.dacd.lastfm.model.Track;
import es.ulpgc.dacd.lastfm.serializer.TrackSerializer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {

    private final LastFmFeeder feeder;
    private final TrackSerializer serializer;

    public Controller(LastFmFeeder feeder, TrackSerializer serializer) {
        this.feeder = feeder;
        this.serializer = serializer;
    }

    public void start() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::run, 0, 6, TimeUnit.HOURS);
    }

    private void run() {
        List<Track> tracks = feeder.feed();
        for (Track track : tracks) {
            serializer.serialize(track);
        }
    }
}
