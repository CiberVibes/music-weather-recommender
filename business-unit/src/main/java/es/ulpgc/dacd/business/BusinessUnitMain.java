package es.ulpgc.dacd.business;

import es.ulpgc.dacd.business.controller.Controller;
import es.ulpgc.dacd.business.datamart.TrackDatamart;
import es.ulpgc.dacd.business.handler.TrackEventHandler;
import es.ulpgc.dacd.business.subscriber.JmsSubscriber;

import javax.jms.JMSException;
import java.util.List;

public class BusinessUnitMain {

    public static void main(String[] args) throws JMSException {
        String brokerUrl = args[0];
        String datamartPath = args[1];

        TrackDatamart datamart = new TrackDatamart(datamartPath);
        TrackEventHandler trackHandler = new TrackEventHandler(datamart);

        List<JmsSubscriber> subscribers = List.of(
                new JmsSubscriber("Track", trackHandler)
        );

        new Controller(brokerUrl, subscribers).start();
    }
}
