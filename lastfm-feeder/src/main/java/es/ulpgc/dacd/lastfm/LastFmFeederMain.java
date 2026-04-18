package es.ulpgc.dacd.lastfm;

import es.ulpgc.dacd.lastfm.controller.LastFmController;

public class LastFmFeederMain {

    public static void main(String[] args) {
        new LastFmController().start();
    }
}
