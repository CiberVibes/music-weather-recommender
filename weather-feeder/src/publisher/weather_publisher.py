from abc import ABC, abstractmethod
from src.model import Weather


class WeatherPublisher(ABC):
    @abstractmethod
    def publish(self, weather: Weather) -> None:
        pass
