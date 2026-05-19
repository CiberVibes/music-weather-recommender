from abc import ABC, abstractmethod
from ..model.weather import Weather


class WeatherPublisher(ABC):
    @abstractmethod
    def publish(self, weather: Weather) -> None:
        pass
