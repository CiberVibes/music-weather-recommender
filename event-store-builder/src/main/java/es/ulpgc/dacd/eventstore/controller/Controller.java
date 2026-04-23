package es.ulpgc.dacd.eventstore.controller;

import es.ulpgc.dacd.eventstore.subscriber.JmsSubscriber;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.List;

public class Controller {

    private final String brokerUrl;
    private final List<JmsSubscriber> subscribers;

    public Controller(String brokerUrl, List<JmsSubscriber> subscribers) {
        this.brokerUrl = brokerUrl;
        this.subscribers = subscribers;
    }

    public void start() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.setClientID("event-store-builder");
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        for (JmsSubscriber subscriber : subscribers) {
            subscriber.start(session);
        }
    }
}
