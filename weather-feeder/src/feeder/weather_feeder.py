from abc import ABC, abstractmethod
from typing import List
from src.model import Weather


class WeatherFeeder(ABC):
    @abstractmethod
    def feed(self):
        pass
