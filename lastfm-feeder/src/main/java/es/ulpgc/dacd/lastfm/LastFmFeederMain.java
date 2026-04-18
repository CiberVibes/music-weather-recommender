package es.ulpgc.dacd.lastfm;

import es.ulpgc.dacd.lastfm.controller.Controller;
import es.ulpgc.dacd.lastfm.feeder.LastFmApiFeeder;
import es.ulpgc.dacd.lastfm.serializer.DatabaseTrackSerializer;

public class LastFmFeederMain {

    public static void main(String[] args) {
        String apiKey = args[0];
        String country = args[1];
        String dbPath = args[2];

        LastFmApiFeeder feeder = new LastFmApiFeeder(apiKey, country);
        DatabaseTrackSerializer serializer = new DatabaseTrackSerializer(dbPath);
        new Controller(feeder, serializer).start();
    }
}
