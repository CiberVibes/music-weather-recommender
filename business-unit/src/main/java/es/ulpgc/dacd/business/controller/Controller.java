package es.ulpgc.dacd.business.controller;

import es.ulpgc.dacd.business.handler.EventHandler;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.List;
import java.util.Map;

public class Controller {

    private final String brokerUrl;
    private final List<JmsSubscriber> subscribers;
    private final EventStoreReader eventStoreReader;
    private final Map<String, EventHandler> historicalHandlers;
    private final Runnable postLoadAction;

    public Controller(String brokerUrl, List<JmsSubscriber> subscribers,
                      EventStoreReader eventStoreReader, Map<String, EventHandler> historicalHandlers,
                      Runnable postLoadAction) {
        this.brokerUrl = brokerUrl;
        this.subscribers = subscribers;
        this.eventStoreReader = eventStoreReader;
        this.historicalHandlers = historicalHandlers;
        this.postLoadAction = postLoadAction;
    }

    public void start() throws JMSException {
        loadHistoricalEvents();
        startRealTimeSubscriptions();
    }

    private void loadHistoricalEvents() {
        historicalHandlers.forEach(eventStoreReader::load);
        historicalHandlers.values().forEach(h -> h.setActive(true));
        postLoadAction.run();
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
