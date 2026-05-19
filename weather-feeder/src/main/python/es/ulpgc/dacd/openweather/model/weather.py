from dataclasses import dataclass
from datetime import datetime
from typing import Optional


@dataclass
class Location:
    name: str
    lat: float
    lon: float
    country: str

    def __str__(self):
        return f"Location(name={self.name}, lat={self.lat}, lon={self.lon}, country={self.country})"


@dataclass
class Weather:
    location: Location
    temperature: float
    feels_like: float
    temp_min: float
    temp_max: float
    pressure: int
    humidity: int
    weather_main: str
    weather_description: str
    clouds: int
    wind_speed: float
    rain: Optional[float]
    snow: Optional[float]
    captured_at: datetime

    def __str__(self):
        return (f"Weather(location={self.location.name}, "
                f"temp={self.temperature}, "
                f"description={self.weather_description}, "
                f"captured_at={self.captured_at})")
