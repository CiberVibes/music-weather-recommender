package es.ulpgc.dacd.business.subscriber;

import es.ulpgc.dacd.business.handler.EventHandler;

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
            if (message instanceof TextMessage textMessage) {
                handler.handle(textMessage.getText());
            }
        } catch (JMSException e) {
            logger.severe("Failed to process message from topic '" + topicName + "': " + e.getMessage());
        }
    }
}
