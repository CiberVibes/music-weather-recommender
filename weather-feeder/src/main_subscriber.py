import sys
import time
from src.datamart import WeatherDatamart
from src.subscriber import ActiveMQWeatherSubscriber


def main():
    if len(sys.argv) < 2:
        print("Usage: python main_subscriber.py <db_path>")
        print("Example: python main_subscriber.py weather_datamart.db")
        sys.exit(1)

    db_path = sys.argv[1]
    datamart = WeatherDatamart(db_path)
    subscriber = ActiveMQWeatherSubscriber(datamart)

    print("Starting Weather Subscriber...")
    subscriber.start()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        subscriber.stop()
        print("Subscriber stopped")


if __name__ == "__main__":
    main()
