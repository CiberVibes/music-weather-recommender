package es.ulpgc.dacd.lastfm;

import es.ulpgc.dacd.lastfm.controller.Controller;
import es.ulpgc.dacd.lastfm.controller.GsonTrackEventSerializer;
import es.ulpgc.dacd.lastfm.controller.JmsTrackPublisher;
import es.ulpgc.dacd.lastfm.controller.LastFmApiFeeder;
import es.ulpgc.dacd.lastfm.controller.TrackEventSerializer;

import javax.jms.JMSException;

public class LastFmFeederMain {

    public static void main(String[] args) throws JMSException {
        String apiKey = args[0];
        String country = args[1];
        String brokerUrl = args[2];

        LastFmApiFeeder feeder = new LastFmApiFeeder(apiKey, country);
        TrackEventSerializer eventSerializer = new GsonTrackEventSerializer("lastfm-feeder");
        JmsTrackPublisher publisher = new JmsTrackPublisher(brokerUrl, eventSerializer);
        new Controller(feeder, publisher).start();
    }
}
