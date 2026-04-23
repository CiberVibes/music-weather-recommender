import requests
from datetime import datetime
from typing import List, Dict, Any
from urllib.parse import urlencode
from src.feeder.weather_feeder import WeatherFeeder
from src.model import Weather, Location


class OpenWeatherMapFeeder(WeatherFeeder):
    BASE_URL = "https://api.openweathermap.org/data/2.5/weather"

    def __init__(self, api_key: str, locations: List[Dict[str, float]]):
        self.api_key = api_key
        self.locations = locations

    def feed(self) -> List[Weather]:
        weathers = []
        for location in self.locations:
            try:
                weather = self._fetch_weather(location)
                weathers.append(weather)
            except Exception as e:
                print(f"Error fetching weather for {location}: {e}")
        return weathers

    def _fetch_weather(self, location: Dict[str, float]) -> Weather:
        params = {
            'lat': location['lat'],
            'lon': location['lon'],
            'appid': self.api_key,
            'units': 'metric'
        }

        response = requests.get(self.BASE_URL, params=params, timeout=10)

        if not response.ok:
            raise Exception(f"HTTP Error: {response.status_code} - {response.text}")

        data = response.json()
        return self._parse_weather(data)

    def _parse_weather(self, data: Dict[str, Any]) -> Weather:
        captured_at = datetime.now()

        location = Location(
            name=data['name'],
            lat=data['coord']['lat'],
            lon=data['coord']['lon'],
            country=data['sys']['country']
        )

        weather = Weather(
            location=location,
            temperature=data['main']['temp'],
            feels_like=data['main']['feels_like'],
            temp_min=data['main']['temp_min'],
            temp_max=data['main']['temp_max'],
            pressure=data['main']['pressure'],
            humidity=data['main']['humidity'],
            weather_main=data['weather'][0]['main'],
            weather_description=data['weather'][0]['description'],
            clouds=data['clouds']['all'],
            wind_speed=data['wind']['speed'],
            rain=data.get('rain', {}).get('1h'),
            snow=data.get('snow', {}).get('1h'),
            captured_at=captured_at
        )

        return weather
