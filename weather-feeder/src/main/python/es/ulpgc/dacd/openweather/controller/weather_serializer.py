from abc import ABC, abstractmethod
from ..model.weather import Weather


class WeatherSerializer(ABC):
    @abstractmethod
    def serialize(self, weather: Weather) -> None:
        pass
