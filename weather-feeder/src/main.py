import sys
import json
import os
from src.controller import Controller
from src.feeder import OpenWeatherMapFeeder
from src.serializer import DatabaseWeatherSerializer
from src.publisher import ActiveMQWeatherPublisher


def load_locations():
    config_file = "locations.json"
    if os.path.exists(config_file):
        with open(config_file, 'r') as f:
            data = json.load(f)
            return data
    else:
        # default locations if file not found
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
    if len(sys.argv) < 3:
        print("Usage: python main.py <api_key> <db_path> [interval_hours]")
        print("Example: python main.py YOUR_API_KEY weather.db 1")
        sys.exit(1)

    api_key = sys.argv[1]
    db_path = sys.argv[2]
    interval_hours = int(sys.argv[3]) if len(sys.argv) > 3 else 1

    locations = load_locations()
    print(f"Loaded {len(locations)} locations")

    feeder = OpenWeatherMapFeeder(api_key, locations)
    serializer = DatabaseWeatherSerializer(db_path)
    publisher = ActiveMQWeatherPublisher()
    publisher.connect()
    controller = Controller(feeder, serializer, publisher)

    print(f"Starting Weather Feeder with interval of {interval_hours} hour(s)")
    controller.start(interval_hours)


if __name__ == "__main__":
    main()
