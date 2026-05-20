package es.ulpgc.dacd.eventstore.controller;

import javax.jms.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class JmsSubscriber {

    private static final Logger logger = Logger.getLogger(JmsSubscriber.class.getName());

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
            String text = extractText(message);
            if (text != null) eventStore.save(topicName, text);
        } catch (JMSException e) {
            logger.severe("Failed to process message from topic '" + topicName + "': " + e.getMessage());
        }
    }

    private String extractText(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) return textMessage.getText();
        if (message instanceof BytesMessage bytesMessage) {
            byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        }
        logger.warning("Unexpected message type: " + message.getClass().getName());
        return null;
    }
}
