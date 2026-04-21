package es.ulpgc.dacd.eventstore.controller;

import es.ulpgc.dacd.eventstore.subscriber.JmsSubscriber;

import javax.jms.JMSException;
import java.util.List;

public class Controller {

    private final List<JmsSubscriber> subscribers;

    public Controller(List<JmsSubscriber> subscribers) {
        this.subscribers = subscribers;
    }

    public void start() throws JMSException {
        for (JmsSubscriber subscriber : subscribers) {
            subscriber.start();
        }
    }
}
