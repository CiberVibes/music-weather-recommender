import logging
import time
from threading import Thread
from src.feeder import WeatherFeeder
from src.serializer import WeatherSerializer
from src.publisher import WeatherPublisher

logger = logging.getLogger(__name__)


class Controller:
    def __init__(self, feeder: WeatherFeeder, serializer: WeatherSerializer, publisher: WeatherPublisher = None):
        self.feeder = feeder
        self.serializer = serializer
        self.publisher = publisher
        self.running = False

    def start(self, interval_hours: int = 1):
        self.running = True
        self.run()

        def scheduler():
            while self.running:
                time.sleep(interval_hours * 3600)
                if self.running:
                    self.run()

        thread = Thread(target=scheduler, daemon=True)
        thread.start()

        try:
            while self.running:
                time.sleep(1)
        except KeyboardInterrupt:
            self.stop()

    def stop(self):
        self.running = False
        logger.info("Controller stopped")

    def run(self):
        try:
            logger.info("Fetching weather data...")
            weathers = self.feeder.feed()
            logger.info(f"Fetched {len(weathers)} weather records")

            count = 0
            for weather in weathers:
                self.serializer.serialize(weather)
                if self.publisher:
                    self.publisher.publish(weather)
                count += 1

            logger.info(f"Saved {count} weather records to database")
            if self.publisher:
                logger.info(f"Published {count} weather records to ActiveMQ")
        except Exception as e:
            logger.error(f"Error during processing: {e}")
