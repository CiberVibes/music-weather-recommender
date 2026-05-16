import stomp
import json
from src.publisher import WeatherPublisher
from src.model import Weather


class ActiveMQWeatherPublisher(WeatherPublisher):
    def __init__(self, host='localhost', port=61613, topic='/topic/weather'):
        self.host = host
        self.port = port
        self.topic = topic
        self.conn = None

    def connect(self):
        self.conn = stomp.Connection([(self.host, self.port)])
        self.conn.connect('admin', 'admin', wait=True)

    def publish(self, weather: Weather) -> None:
        if not self.conn or not self.conn.is_connected():
            self.connect()

        weather_data = {
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
            'snow': weather.snow,
            'captured_at': weather.captured_at.isoformat()
        }

        message = json.dumps(weather_data)
        self.conn.send(body=message, destination=self.topic)

    def disconnect(self):
        if self.conn and self.conn.is_connected():
            self.conn.disconnect()
