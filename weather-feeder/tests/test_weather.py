import unittest
from datetime import datetime
from src.model import Weather, Location


class TestLocation(unittest.TestCase):
    def test_create_location(self):
        location = Location(
            name="Test City",
            lat=28.1,
            lon=-15.4,
            country="ES"
        )
        self.assertEqual(location.name, "Test City")
        self.assertEqual(location.lat, 28.1)
        self.assertEqual(location.lon, -15.4)
        self.assertEqual(location.country, "ES")


class TestWeather(unittest.TestCase):
    def setUp(self):
        self.location = Location("Test City", 28.1, -15.4, "ES")

    def test_create_weather(self):
        weather = Weather(
            location=self.location,
            temperature=20.5,
            feels_like=19.8,
            temp_min=18.0,
            temp_max=22.0,
            pressure=1013,
            humidity=65,
            weather_main="Clear",
            weather_description="clear sky",
            clouds=10,
            wind_speed=3.5,
            rain=None,
            snow=None,
            captured_at=datetime.now()
        )

        self.assertEqual(weather.location.name, "Test City")
        self.assertEqual(weather.temperature, 20.5)
        self.assertEqual(weather.humidity, 65)
        self.assertIsNone(weather.rain)


if __name__ == '__main__':
    unittest.main()
