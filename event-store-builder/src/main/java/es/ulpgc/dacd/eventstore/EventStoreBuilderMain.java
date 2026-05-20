package es.ulpgc.dacd.eventstore;

import es.ulpgc.dacd.eventstore.control.Controller;
import es.ulpgc.dacd.eventstore.control.FileEventStore;
import es.ulpgc.dacd.eventstore.control.JmsSubscriber;

import javax.jms.JMSException;
import java.util.List;

public class EventStoreBuilderMain {

    public static void main(String[] args) throws JMSException {
        String brokerUrl = args[0];
        String eventStorePath = args[1];

        FileEventStore eventStore = new FileEventStore(eventStorePath);

        List<JmsSubscriber> subscribers = List.of(
                new JmsSubscriber("Track", eventStore),
                new JmsSubscriber("Weather", eventStore)
        );

        new Controller(brokerUrl, subscribers).start();
    }
}
