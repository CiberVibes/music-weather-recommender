import json
import logging
import stomp
from datamart import WeatherDatamart
from subscriber.weather_subscriber import WeatherSubscriber

logger = logging.getLogger(__name__)


class WeatherListener(stomp.ConnectionListener):
    def __init__(self, datamart: WeatherDatamart):
        self.datamart = datamart

    def on_message(self, frame):
        try:
            event = json.loads(frame.body)
            self.datamart.save(event)
            location = event.get('location', {}).get('name', 'unknown')
            logger.info(f"Saved weather event for {location}")
        except Exception as e:
            logger.error(f"Error processing message: {e}")


class ActiveMQWeatherSubscriber(WeatherSubscriber):
    def __init__(self, datamart: WeatherDatamart, host='localhost', port=61613, topic='/topic/Weather'):
        self.datamart = datamart
        self.host = host
        self.port = port
        self.topic = topic
        self.conn = None

    def start(self) -> None:
        self.conn = stomp.Connection([(self.host, self.port)], client_id='weather-subscriber')
        self.conn.set_listener('', WeatherListener(self.datamart))
        self.conn.connect('admin', 'admin', wait=True)
        self.conn.subscribe(
            destination=self.topic,
            id='weather-sub',
            ack='auto',
            headers={'activemq.subscriptionName': 'weather-datamart-sub'}
        )
        logger.info(f"Subscribed to {self.topic}")

    def stop(self) -> None:
        if self.conn and self.conn.is_connected():
            self.conn.disconnect()
