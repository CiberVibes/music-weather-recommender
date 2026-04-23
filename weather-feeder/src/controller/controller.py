import time
from threading import Thread
from src.feeder.weather_feeder import WeatherFeeder
from src.serializer.weather_serializer import WeatherSerializer


class Controller:
    def __init__(self, feeder: WeatherFeeder, serializer: WeatherSerializer):
        self.feeder = feeder
        self.serializer = serializer
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
        print("Controller stopped")

    def run(self):
        try:
            print("Fetching weather data...")
            weathers = self.feeder.feed()
            print(f"Fetched {len(weathers)} weather records")

            count = 0
            for weather in weathers:
                self.serializer.serialize(weather)
                count += 1

            print(f"Saved {count} weather records to database")
        except Exception as e:
            print(f"Error during processing: {e}")
