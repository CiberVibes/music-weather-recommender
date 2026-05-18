import json
import unittest
from datetime import datetime, timezone
from unittest.mock import MagicMock, patch
from src.model import Weather, Location
from src.publisher import ActiveMQWeatherPublisher


def make_weather():
    location = Location("Las Palmas", 28.1, -15.4, "ES")
    return Weather(
        location=location,
        temperature=22.0,
        feels_like=21.0,
        temp_min=20.0,
        temp_max=24.0,
        pressure=1015,
        humidity=60,
        weather_main="Clear",
        weather_description="clear sky",
        clouds=5,
        wind_speed=3.0,
        rain=None,
        snow=None,
        captured_at=datetime(2024, 11, 3, 12, 0, 0, tzinfo=timezone.utc)
    )


class TestActiveMQWeatherPublisher(unittest.TestCase):

    def test_to_dict_contains_ts_and_ss(self):
        publisher = ActiveMQWeatherPublisher()
        weather = make_weather()
        result = publisher._to_dict(weather)
        self.assertIn('ts', result)
        self.assertIn('ss', result)
        self.assertEqual(result['ss'], 'OpenWeatherMap')

    def test_to_dict_ts_matches_captured_at(self):
        publisher = ActiveMQWeatherPublisher()
        weather = make_weather()
        result = publisher._to_dict(weather)
        self.assertEqual(result['ts'], weather.captured_at.isoformat())

    def test_to_dict_contains_weather_fields(self):
        publisher = ActiveMQWeatherPublisher()
        weather = make_weather()
        result = publisher._to_dict(weather)
        self.assertEqual(result['temperature'], 22.0)
        self.assertEqual(result['humidity'], 60)
        self.assertEqual(result['weather_main'], 'Clear')

    def test_to_dict_does_not_contain_captured_at(self):
        publisher = ActiveMQWeatherPublisher()
        weather = make_weather()
        result = publisher._to_dict(weather)
        self.assertNotIn('captured_at', result)

    @patch('src.publisher.activemq_weather_publisher.stomp.Connection')
    def test_publish_sends_valid_json(self, mock_connection_class):
        mock_conn = MagicMock()
        mock_conn.is_connected.return_value = True
        mock_connection_class.return_value = mock_conn

        publisher = ActiveMQWeatherPublisher()
        publisher.conn = mock_conn

        publisher.publish(make_weather())

        mock_conn.send.assert_called_once()
        call_kwargs = mock_conn.send.call_args
        body = call_kwargs.kwargs.get('body') or call_kwargs.args[0]
        parsed = json.loads(body)
        self.assertIn('ts', parsed)
        self.assertIn('ss', parsed)

    @patch('src.publisher.activemq_weather_publisher.stomp.Connection')
    def test_publish_retries_on_failure(self, mock_connection_class):
        mock_conn = MagicMock()
        mock_conn.is_connected.return_value = True
        mock_conn.send.side_effect = [Exception("broker error"), Exception("broker error"), None]
        mock_connection_class.return_value = mock_conn

        publisher = ActiveMQWeatherPublisher()
        publisher.conn = mock_conn

        with patch('src.publisher.activemq_weather_publisher.time.sleep'):
            publisher.publish(make_weather())

        self.assertEqual(mock_conn.send.call_count, 3)

    @patch('src.publisher.activemq_weather_publisher.stomp.Connection')
    def test_publish_reconnects_if_disconnected(self, mock_connection_class):
        mock_conn = MagicMock()
        mock_conn.is_connected.return_value = False
        mock_connection_class.return_value = mock_conn

        publisher = ActiveMQWeatherPublisher()
        publisher.conn = mock_conn

        publisher.publish(make_weather())

        mock_conn.connect.assert_called_once()


if __name__ == '__main__':
    unittest.main()
