import sys
import json
import os
from .controller.controller import Controller
from .controller.openweathermap_feeder import OpenWeatherMapFeeder
from .controller.activemq_weather_publisher import ActiveMQWeatherPublisher


def load_locations():
    config_file = "locations.json"
    if os.path.exists(config_file):
        with open(config_file, 'r') as f:
            return json.load(f)
    return [
        {"lat": 28.1, "lon": -15.4},
        {"lat": 28.5, "lon": -16.3},
        {"lat": 29.0, "lon": -13.6},
        {"lat": 28.3, "lon": -14.0},
        {"lat": 28.7, "lon": -17.9},
        {"lat": 27.8, "lon": -15.6},
        {"lat": 28.0, "lon": -16.7},
        {"lat": 29.2, "lon": -13.5}
    ]


def main():
    if len(sys.argv) < 2:
        print("Usage: python -m es.ulpgc.dacd.openweather.main <api_key> [interval_hours]")
        sys.exit(1)

    api_key = sys.argv[1]
    interval_hours = int(sys.argv[2]) if len(sys.argv) > 2 else 1

    locations = load_locations()
    print(f"Loaded {len(locations)} locations")

    feeder = OpenWeatherMapFeeder(api_key, locations)
    publisher = ActiveMQWeatherPublisher()
    publisher.connect()
    controller = Controller(feeder, None, publisher)

    print(f"Starting Weather Feeder with interval of {interval_hours} hour(s)")
    try:
        controller.start(interval_hours)
    finally:
        publisher.disconnect()
        print("Publisher disconnected")


if __name__ == "__main__":
    main()
