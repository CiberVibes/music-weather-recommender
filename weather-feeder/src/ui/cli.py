from src.datamart import WeatherDatamart


class Cli:
    def __init__(self, datamart: WeatherDatamart):
        self.datamart = datamart

    def start(self) -> None:
        print("\n=== Weather Datamart CLI ===")
        while True:
            print("\n1. Show latest weather for all locations")
            print("2. Show weather for a specific location")
            print("3. Exit")
            choice = input("Select option: ").strip()

            if choice == '1':
                self._show_all_latest()
            elif choice == '2':
                location = input("Enter location name: ").strip()
                self._show_location(location)
            elif choice == '3':
                break

    def _show_all_latest(self) -> None:
        records = self.datamart.get_all_latest()
        if not records:
            print("No weather data available.")
            return
        print(f"\n{'Location':<20} {'Condition':<15} {'Temp (°C)':<12} {'Humidity'}")
        print("─" * 60)
        for r in records:
            print(f"{r['location_name']:<20} {r['weather_main']:<15} {r['temperature']:<12} {r['humidity']}%")

    def _show_location(self, location_name: str) -> None:
        record = self.datamart.get_latest_by_location(location_name)
        if not record:
            print(f"No data found for '{location_name}'.")
            return
        print(f"\nLocation:    {record['location_name']} ({record['country']})")
        print(f"Condition:   {record['weather_main']} — {record['weather_description']}")
        print(f"Temperature: {record['temperature']}°C (feels like {record['feels_like']}°C)")
        print(f"Humidity:    {record['humidity']}%")
        print(f"Wind:        {record['wind_speed']} m/s")
        print(f"Last updated:{record['ts']}")
