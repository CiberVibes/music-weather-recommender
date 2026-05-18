import sqlite3


class WeatherDatamart:
    def __init__(self, db_path: str):
        self.db_path = db_path
        self._init_db()

    def _init_db(self):
        with self._connect() as conn:
            conn.execute("""
                CREATE TABLE IF NOT EXISTS weather (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ts TEXT NOT NULL,
                    ss TEXT NOT NULL,
                    location_name TEXT,
                    lat REAL,
                    lon REAL,
                    country TEXT,
                    temperature REAL,
                    feels_like REAL,
                    humidity INTEGER,
                    weather_main TEXT,
                    weather_description TEXT,
                    wind_speed REAL,
                    clouds INTEGER,
                    rain REAL,
                    snow REAL
                )
            """)

    def save(self, event: dict):
        location = event.get('location', {})
        with self._connect() as conn:
            conn.execute("""
                INSERT INTO weather
                (ts, ss, location_name, lat, lon, country, temperature, feels_like,
                 humidity, weather_main, weather_description, wind_speed, clouds, rain, snow)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (
                event.get('ts'),
                event.get('ss'),
                location.get('name'),
                location.get('lat'),
                location.get('lon'),
                location.get('country'),
                event.get('temperature'),
                event.get('feels_like'),
                event.get('humidity'),
                event.get('weather_main'),
                event.get('weather_description'),
                event.get('wind_speed'),
                event.get('clouds'),
                event.get('rain'),
                event.get('snow')
            ))

    def get_latest_by_location(self, location_name: str) -> dict | None:
        with self._connect() as conn:
            conn.row_factory = sqlite3.Row
            cursor = conn.execute("""
                SELECT * FROM weather
                WHERE location_name = ?
                ORDER BY ts DESC LIMIT 1
            """, (location_name,))
            row = cursor.fetchone()
            return dict(row) if row else None

    def get_all_latest(self) -> list[dict]:
        with self._connect() as conn:
            conn.row_factory = sqlite3.Row
            cursor = conn.execute("""
                SELECT w.* FROM weather w
                INNER JOIN (
                    SELECT location_name, MAX(ts) AS max_ts
                    FROM weather
                    GROUP BY location_name
                ) latest ON w.location_name = latest.location_name
                         AND w.ts = latest.max_ts
                ORDER BY w.location_name
            """)
            return [dict(row) for row in cursor.fetchall()]

    def _connect(self) -> sqlite3.Connection:
        return sqlite3.connect(self.db_path)
