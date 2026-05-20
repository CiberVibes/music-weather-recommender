from abc import ABC, abstractmethod
from typing import List
from ..model.weather import Weather


class WeatherFeeder(ABC):
    @abstractmethod
    def feed(self):
        pass
