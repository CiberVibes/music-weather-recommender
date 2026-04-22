package es.ulpgc.dacd.eventstore;

import es.ulpgc.dacd.eventstore.controller.Controller;
import es.ulpgc.dacd.eventstore.store.FileEventStore;
import es.ulpgc.dacd.eventstore.subscriber.JmsSubscriber;

import javax.jms.JMSException;
import java.util.List;

public class EventStoreBuilderMain {

    public static void main(String[] args) throws JMSException {
        String brokerUrl = args[0];
        String eventStorePath = args[1];

        FileEventStore eventStore = new FileEventStore(eventStorePath);

        List<JmsSubscriber> subscribers = List.of(
                new JmsSubscriber(brokerUrl, "Track", eventStore),
                new JmsSubscriber(brokerUrl, "Weather", eventStore)
        );

        new Controller(subscribers).start();
    }
}
