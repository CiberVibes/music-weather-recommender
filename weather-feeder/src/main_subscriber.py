import json
import sys
from src.datamart import WeatherDatamart
from src.store import EventStoreReader
from src.subscriber import ActiveMQWeatherSubscriber
from src.ui import Cli


def main():
    if len(sys.argv) < 3:
        print("Usage: python main_subscriber.py <db_path> <event_store_path>")
        print("Example: python main_subscriber.py weather_datamart.db eventstore")
        sys.exit(1)

    db_path = sys.argv[1]
    event_store_path = sys.argv[2]

    datamart = WeatherDatamart(db_path)

    print("Loading historical weather events...")
    reader = EventStoreReader(event_store_path)
    reader.load('Weather', lambda line: datamart.save(json.loads(line)))
    print("Historical events loaded.")

    subscriber = ActiveMQWeatherSubscriber(datamart)
    subscriber.start()

    try:
        Cli(datamart).start()
    finally:
        subscriber.stop()
        print("Subscriber stopped.")


if __name__ == "__main__":
    main()
