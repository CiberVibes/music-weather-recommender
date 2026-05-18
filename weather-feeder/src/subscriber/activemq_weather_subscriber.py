import json
import stomp
from src.datamart import WeatherDatamart
from src.subscriber.weather_subscriber import WeatherSubscriber


class WeatherListener(stomp.ConnectionListener):
    def __init__(self, datamart: WeatherDatamart):
        self.datamart = datamart

    def on_message(self, frame):
        try:
            event = json.loads(frame.body)
            self.datamart.save(event)
            location = event.get('location', {}).get('name', 'unknown')
            print(f"[subscriber] Saved weather event for {location}")
        except Exception as e:
            print(f"[subscriber] Error processing message: {e}")


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
        print(f"[subscriber] Subscribed to {self.topic}")

    def stop(self) -> None:
        if self.conn and self.conn.is_connected():
            self.conn.disconnect()
