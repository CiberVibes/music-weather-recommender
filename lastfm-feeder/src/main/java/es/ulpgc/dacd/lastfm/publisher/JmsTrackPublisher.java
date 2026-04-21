package es.ulpgc.dacd.lastfm.publisher;

import com.google.gson.Gson;
import es.ulpgc.dacd.lastfm.model.Track;
import es.ulpgc.dacd.lastfm.model.TrackEvent;
import es.ulpgc.dacd.lastfm.serializer.TrackSerializer;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class JmsTrackPublisher implements TrackSerializer {

    private static final String TOPIC = "Track";

    private final String ss;
    private final Gson gson;
    private final Session session;
    private final MessageProducer producer;

    public JmsTrackPublisher(String brokerUrl, String ss) throws JMSException {
        this.ss = ss;
        this.gson = new Gson();
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.start();
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        this.producer = session.createProducer(session.createTopic(TOPIC));
    }

    @Override
    public void serialize(Track track) {
        try {
            TrackEvent event = new TrackEvent(ss, track);
            String json = gson.toJson(event);
            producer.send(session.createTextMessage(json));
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
