package es.ulpgc.dacd.business;

import es.ulpgc.dacd.business.controller.Controller;
import es.ulpgc.dacd.business.datamart.TrackDatamart;
import es.ulpgc.dacd.business.handler.EventHandler;
import es.ulpgc.dacd.business.handler.TrackEventHandler;
import es.ulpgc.dacd.business.recommendation.TrackRecommender;
import es.ulpgc.dacd.business.store.EventStoreReader;
import es.ulpgc.dacd.business.subscriber.JmsSubscriber;
import es.ulpgc.dacd.business.ui.Cli;

import javax.jms.JMSException;
import java.util.List;
import java.util.Map;

public class BusinessUnitMain {

    public static void main(String[] args) throws JMSException {
        String brokerUrl = args[0];
        String eventStorePath = args[1];
        String datamartPath = args[2];

        TrackDatamart datamart = new TrackDatamart(datamartPath);
        TrackEventHandler trackHandler = new TrackEventHandler(datamart);

        List<JmsSubscriber> subscribers = List.of(
                new JmsSubscriber("Track", trackHandler)
        );

        Map<String, EventHandler> historicalHandlers = Map.of(
                "Track", trackHandler
        );

        EventStoreReader eventStoreReader = new EventStoreReader(eventStorePath);
        new Controller(brokerUrl, subscribers, eventStoreReader, historicalHandlers).start();

        new Cli(new TrackRecommender(datamart)).start();
    }
}
