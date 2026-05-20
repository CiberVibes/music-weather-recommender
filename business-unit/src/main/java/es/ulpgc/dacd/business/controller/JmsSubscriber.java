package es.ulpgc.dacd.business.controller;

import javax.jms.*;
import java.util.logging.Logger;

public class JmsSubscriber {

    private static final Logger logger = Logger.getLogger(JmsSubscriber.class.getName());

    private final String topicName;
    private final EventHandler handler;

    public JmsSubscriber(String topicName, EventHandler handler) {
        this.topicName = topicName;
        this.handler = handler;
    }

    public void start(Session session) throws JMSException {
        Topic topic = session.createTopic(topicName);
        MessageConsumer consumer = session.createDurableSubscriber(topic, topicName + "-business-unit-sub");
        consumer.setMessageListener(this::onMessage);
    }

    private void onMessage(Message message) {
        try {
            String text = extractText(message);
            if (text != null) handler.handle(text);
        } catch (JMSException e) {
            logger.severe("Failed to process message from topic '" + topicName + "': " + e.getMessage());
        }
    }

    private String extractText(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            return textMessage.getText();
        }
        if (message instanceof BytesMessage bytesMessage) {
            byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytes);
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        }
        logger.warning("Unexpected message type: " + message.getClass().getName());
        return null;
    }
}
