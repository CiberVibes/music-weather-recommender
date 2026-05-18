import unittest
import os
import sqlite3
import time
from datetime import datetime
from src.serializer import DatabaseWeatherSerializer
from src.model import Weather, Location


class TestDatabaseWeatherSerializer(unittest.TestCase):
    def setUp(self):
        self.test_db = "test_weather.db"
        if os.path.exists(self.test_db):
            try:
                os.remove(self.test_db)
            except PermissionError:
                time.sleep(0.1)
        self.serializer = DatabaseWeatherSerializer(self.test_db)

    def tearDown(self):
        del self.serializer
        time.sleep(0.1)

    def test_table_creation(self):
        conn = sqlite3.connect(self.test_db)
        cursor = conn.cursor()
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='weather'")
        result = cursor.fetchone()
        conn.close()

        self.assertIsNotNone(result)

    def test_serialize_weather(self):
        location = Location("Test City", 28.1, -15.4, "ES")
        weather = Weather(
            location=location,
            temperature=20.0,
            feels_like=19.0,
            temp_min=18.0,
            temp_max=22.0,
            pressure=1013,
            humidity=60,
            weather_main="Clear",
            weather_description="clear sky",
            clouds=0,
            wind_speed=2.5,
            rain=None,
            snow=None,
            captured_at=datetime.now()
        )

        self.serializer.serialize(weather)

        conn = sqlite3.connect(self.test_db)
        cursor = conn.cursor()
        cursor.execute("SELECT COUNT(*) FROM weather")
        count = cursor.fetchone()[0]
        conn.close()

        self.assertEqual(count, 1)


if __name__ == '__main__':
    unittest.main()
