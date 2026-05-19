import sqlite3
from .weather_serializer import WeatherSerializer
from ..model.weather import Weather


class DatabaseWeatherSerializer(WeatherSerializer):
    def __init__(self, db_path: str):
        self.db_path = db_path
        self._create_table()

    def _create_table(self):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS weather (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    location_name TEXT NOT NULL,
                    latitude REAL NOT NULL,
                    longitude REAL NOT NULL,
                    country TEXT NOT NULL,
                    temperature REAL NOT NULL,
                    feels_like REAL NOT NULL,
                    temp_min REAL NOT NULL,
                    temp_max REAL NOT NULL,
                    pressure INTEGER NOT NULL,
                    humidity INTEGER NOT NULL,
                    weather_main TEXT NOT NULL,
                    weather_description TEXT NOT NULL,
                    clouds INTEGER NOT NULL,
                    wind_speed REAL NOT NULL,
                    rain REAL,
                    snow REAL,
                    captured_at TEXT NOT NULL
                )
            ''')
            conn.commit()

    def serialize(self, weather: Weather) -> None:
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            data = self._prepare_data(weather)
            cursor.execute('''
                INSERT INTO weather (
                    location_name, latitude, longitude, country,
                    temperature, feels_like, temp_min, temp_max,
                    pressure, humidity, weather_main, weather_description,
                    clouds, wind_speed, rain, snow, captured_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ''', data)
            conn.commit()

    def _prepare_data(self, weather: Weather):
        return (
            weather.location.name,
            weather.location.lat,
            weather.location.lon,
            weather.location.country,
            weather.temperature,
            weather.feels_like,
            weather.temp_min,
            weather.temp_max,
            weather.pressure,
            weather.humidity,
            weather.weather_main,
            weather.weather_description,
            weather.clouds,
            weather.wind_speed,
            weather.rain,
            weather.snow,
            weather.captured_at.isoformat()
        )
