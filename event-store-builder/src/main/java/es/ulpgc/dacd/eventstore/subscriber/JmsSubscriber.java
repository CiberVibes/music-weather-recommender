package es.ulpgc.dacd.eventstore.subscriber;

import es.ulpgc.dacd.eventstore.store.FileEventStore;

import javax.jms.*;

public class JmsSubscriber {

    private final String topicName;
    private final FileEventStore eventStore;

    public JmsSubscriber(String topicName, FileEventStore eventStore) {
        this.topicName = topicName;
        this.eventStore = eventStore;
    }

    public void start(Session session) throws JMSException {
        Topic topic = session.createTopic(topicName);
        MessageConsumer consumer = session.createDurableSubscriber(topic, topicName + "-sub");
        consumer.setMessageListener(this::onMessage);
    }

    private void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                eventStore.save(topicName, textMessage.getText());
            }
        } catch (JMSException e) {
            System.err.println("[event-store-builder] Failed to process message from topic '" + topicName + "': " + e.getMessage());
        }
    }
}
