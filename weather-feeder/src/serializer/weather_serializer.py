from abc import ABC, abstractmethod
from src.model import Weather


class WeatherSerializer(ABC):
    @abstractmethod
    def serialize(self, weather: Weather) -> None:
        pass
