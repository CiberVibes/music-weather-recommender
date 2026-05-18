import stomp
import json
import time
from src.publisher import WeatherPublisher
from src.model import Weather

MAX_RETRIES = 3
RETRY_DELAY = 5


class ActiveMQWeatherPublisher(WeatherPublisher):
    def __init__(self, host='localhost', port=61613, topic='/topic/Weather'):
        self.host = host
        self.port = port
        self.topic = topic
        self.conn = None

    def connect(self):
        self.conn = stomp.Connection([(self.host, self.port)])
        self.conn.connect('admin', 'admin', wait=True)

    def publish(self, weather: Weather) -> None:
        message = json.dumps(self._to_dict(weather))
        for attempt in range(1, MAX_RETRIES + 1):
            try:
                if not self.conn or not self.conn.is_connected():
                    self.connect()
                self.conn.send(body=message, destination=self.topic)
                return
            except Exception as e:
                print(f"[publisher] Attempt {attempt}/{MAX_RETRIES} failed: {e}")
                if attempt < MAX_RETRIES:
                    time.sleep(RETRY_DELAY)
        print(f"[publisher] Could not publish event after {MAX_RETRIES} attempts")

    def disconnect(self):
        if self.conn and self.conn.is_connected():
            self.conn.disconnect()

    def _to_dict(self, weather: Weather) -> dict:
        return {
            'ts': weather.captured_at.isoformat(),
            'ss': 'OpenWeatherMap',
            'location': {
                'name': weather.location.name,
                'lat': weather.location.lat,
                'lon': weather.location.lon,
                'country': weather.location.country
            },
            'temperature': weather.temperature,
            'feels_like': weather.feels_like,
            'temp_min': weather.temp_min,
            'temp_max': weather.temp_max,
            'pressure': weather.pressure,
            'humidity': weather.humidity,
            'weather_main': weather.weather_main,
            'weather_description': weather.weather_description,
            'clouds': weather.clouds,
            'wind_speed': weather.wind_speed,
            'rain': weather.rain,
            'snow': weather.snow
        }
