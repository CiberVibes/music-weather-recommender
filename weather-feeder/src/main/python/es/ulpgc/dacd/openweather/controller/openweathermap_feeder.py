import requests
from datetime import datetime, timezone
from typing import List, Dict, Any
from urllib.parse import urlencode
from .weather_feeder import WeatherFeeder
from ..model.weather import Weather, Location


class OpenWeatherMapFeeder(WeatherFeeder):
    BASE_URL = "https://api.openweathermap.org/data/2.5/weather"
    UNITS = "metric"
    TIMEOUT = 10

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
        params = self._build_params(location)
        response = requests.get(self.BASE_URL, params=params, timeout=self.TIMEOUT)

        if not response.ok:
            raise Exception(f"HTTP Error: {response.status_code} - {response.text}")

        data = response.json()
        return self._parse_weather(data)

    def _build_params(self, location: Dict[str, float]) -> Dict[str, Any]:
        return {
            'lat': location['lat'],
            'lon': location['lon'],
            'appid': self.api_key,
            'units': self.UNITS
        }

    def _parse_weather(self, data: Dict[str, Any]) -> Weather:
        location = self._parse_location(data)
        return Weather(
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
            captured_at=datetime.now(timezone.utc)
        )

    def _parse_location(self, data: Dict[str, Any]) -> Location:
        return Location(
            name=data['name'],
            lat=data['coord']['lat'],
            lon=data['coord']['lon'],
            country=data['sys']['country']
        )
