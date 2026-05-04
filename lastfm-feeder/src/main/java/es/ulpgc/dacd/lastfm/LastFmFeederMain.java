package es.ulpgc.dacd.lastfm;

import es.ulpgc.dacd.lastfm.controller.Controller;
import es.ulpgc.dacd.lastfm.feeder.LastFmApiFeeder;
import es.ulpgc.dacd.lastfm.publisher.GsonTrackEventSerializer;
import es.ulpgc.dacd.lastfm.publisher.JmsTrackPublisher;
import es.ulpgc.dacd.lastfm.publisher.TrackEventSerializer;

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
