package es.ulpgc.dacd.lastfm.controller;

import es.ulpgc.dacd.lastfm.model.Track;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.logging.Logger;

public class JmsTrackPublisher implements TrackSerializer {

    private static final Logger logger = Logger.getLogger(JmsTrackPublisher.class.getName());

    private static final String TOPIC = "Track";

    private final TrackEventSerializer eventSerializer;
    private final Session session;
    private final MessageProducer producer;

    public JmsTrackPublisher(String brokerUrl, TrackEventSerializer eventSerializer) throws JMSException {
        this.eventSerializer = eventSerializer;
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.start();
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        this.producer = session.createProducer(session.createTopic(TOPIC));
    }

    @Override
    public void serialize(Track track) {
        try {
            String json = eventSerializer.serialize(track);
            producer.send(session.createTextMessage(json));
        } catch (JMSException e) {
            logger.severe("Failed to publish track '" + track.getName() + "': " + e.getMessage());
        }
    }
}
