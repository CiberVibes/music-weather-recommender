package es.ulpgc.dacd.business;

import es.ulpgc.dacd.business.controller.Controller;
import es.ulpgc.dacd.business.controller.EventHandler;
import es.ulpgc.dacd.business.controller.EventStoreReader;
import es.ulpgc.dacd.business.controller.JmsSubscriber;
import es.ulpgc.dacd.business.controller.SpotifyExporter;
import es.ulpgc.dacd.business.controller.TrackDatamart;
import es.ulpgc.dacd.business.controller.TrackEventHandler;
import es.ulpgc.dacd.business.controller.TrackRecommender;
import es.ulpgc.dacd.business.controller.WeatherEventHandler;
import es.ulpgc.dacd.business.controller.WeatherState;
import es.ulpgc.dacd.business.view.WebServer;

import javax.jms.JMSException;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BusinessUnitMain {

    public static void main(String[] args) throws JMSException {
        configureLogging();
        String brokerUrl = args[0];
        String eventStorePath = args[1];
        String datamartPath = args[2];

        TrackDatamart datamart = new TrackDatamart(datamartPath);
        WeatherState weatherState = new WeatherState();
        TrackRecommender recommender = new TrackRecommender(datamart);

        TrackEventHandler trackHandler = new TrackEventHandler(datamart, recommender, weatherState);
        WeatherEventHandler weatherHandler = new WeatherEventHandler(weatherState, recommender);

        List<JmsSubscriber> subscribers = List.of(
                new JmsSubscriber("Track", trackHandler),
                new JmsSubscriber("Weather", weatherHandler)
        );

        Map<String, EventHandler> historicalHandlers = Map.of(
                "Track", trackHandler,
                "Weather", weatherHandler
        );

        EventStoreReader eventStoreReader = new EventStoreReader(eventStorePath);
        Runnable postLoad = () -> recommender.recalculateAll(weatherState.getAll());
        new Controller(brokerUrl, subscribers, eventStoreReader, historicalHandlers, postLoad).start();

        SpotifyExporter spotify = buildSpotifyExporter();
        new WebServer(datamart, weatherState, spotify).start();
    }

    private static SpotifyExporter buildSpotifyExporter() {
        String clientId = System.getProperty("spotify.client.id");
        String clientSecret = System.getProperty("spotify.client.secret");
        if (clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank()) {
            return new SpotifyExporter(clientId, clientSecret);
        }
        return null;
    }

    private static void configureLogging() {
        Logger root = Logger.getLogger("");
        for (var handler : root.getHandlers()) root.removeHandler(handler);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.WARNING);
        root.addHandler(handler);
        root.setLevel(Level.WARNING);
    }
}
