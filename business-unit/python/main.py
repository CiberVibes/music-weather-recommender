import json
import logging
import sys
from datamart import WeatherDatamart
from store import EventStoreReader
from subscriber import ActiveMQWeatherSubscriber
from ui import Cli

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(name)s - %(message)s')
logger = logging.getLogger(__name__)


def load_event(line: str, datamart: WeatherDatamart) -> None:
    datamart.save(json.loads(line))


def main():
    if len(sys.argv) < 3:
        print("Usage: python main.py <db_path> <event_store_path>")
        print("Example: python main.py weather_datamart.db eventstore")
        sys.exit(1)

    db_path = sys.argv[1]
    event_store_path = sys.argv[2]

    datamart = WeatherDatamart(db_path)

    logger.info("Loading historical weather events...")
    reader = EventStoreReader(event_store_path)
    reader.load('Weather', lambda line: load_event(line, datamart))
    logger.info("Historical events loaded.")

    subscriber = ActiveMQWeatherSubscriber(datamart)
    subscriber.start()

    try:
        Cli(datamart).start()
    finally:
        subscriber.stop()
        logger.info("Subscriber stopped.")


if __name__ == "__main__":
    main()
