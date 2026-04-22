package es.ulpgc.dacd.eventstore.subscriber;

import es.ulpgc.dacd.eventstore.store.FileEventStore;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class JmsSubscriber {

    private final String brokerUrl;
    private final String topicName;
    private final FileEventStore eventStore;

    public JmsSubscriber(String brokerUrl, String topicName, FileEventStore eventStore) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.eventStore = eventStore;
    }

    public void start() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.setClientID("event-store-builder");
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicName);
        MessageConsumer consumer = session.createDurableSubscriber(topic, topicName + "-sub");
        consumer.setMessageListener(message -> onMessage(message));
    }

    private void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                eventStore.save(topicName, textMessage.getText());
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
