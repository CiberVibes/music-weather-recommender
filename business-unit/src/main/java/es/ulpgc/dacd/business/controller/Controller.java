package es.ulpgc.dacd.business.controller;

import es.ulpgc.dacd.business.handler.EventHandler;
import es.ulpgc.dacd.business.store.EventStoreReader;
import es.ulpgc.dacd.business.subscriber.JmsSubscriber;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.List;
import java.util.Map;

public class Controller {

    private final String brokerUrl;
    private final List<JmsSubscriber> subscribers;
    private final EventStoreReader eventStoreReader;
    private final Map<String, EventHandler> historicalHandlers;

    public Controller(String brokerUrl, List<JmsSubscriber> subscribers,
                      EventStoreReader eventStoreReader, Map<String, EventHandler> historicalHandlers) {
        this.brokerUrl = brokerUrl;
        this.subscribers = subscribers;
        this.eventStoreReader = eventStoreReader;
        this.historicalHandlers = historicalHandlers;
    }

    public void start() throws JMSException {
        loadHistoricalEvents();
        startRealTimeSubscriptions();
    }

    private void loadHistoricalEvents() {
        historicalHandlers.forEach(eventStoreReader::load);
    }

    private void startRealTimeSubscriptions() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.setClientID("business-unit");
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        for (JmsSubscriber subscriber : subscribers) {
            subscriber.start(session);
        }
    }
}
