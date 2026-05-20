# Music Weather Recommender

Sistema que recomienda música en función del tiempo meteorológico actual y la reproduce directamente en Spotify. Combina datos de canciones populares de Last.fm con datos meteorológicos en tiempo real de OpenWeatherMap para sugerir listas de reproducción adaptadas al estado de ánimo que provoca el clima.

---

## Propuesta de valor

El usuario abre la interfaz web, conecta su cuenta de Spotify Premium y recibe al instante un ranking de canciones cuyo género y etiquetas encajan con el estado de ánimo asociado al clima de su ciudad. Con un solo clic, la canción seleccionada comienza a sonar en su dispositivo Spotify activo. La correspondencia clima → ánimo → música se basa en el modelo circunflejo de Russell (cuadrantes Q1–Q4), ampliamente usado en investigación de Music Emotion Recognition.

| Clima | Ánimo (cuadrante) | Géneros típicos |
|---|---|---|
| Despejado (Clear) | Feliz — Q1 | Pop, dance, disco, funk |
| Nubes (Clouds) | Relajado — Q4 | Ambient, chillout, lo-fi, indie pop |
| Lluvia / Niebla / Nieve | Triste — Q3 | Indie folk, blues, acoustic, post-rock |
| Tormenta (Thunderstorm) | Enérgico/Agresivo — Q2 | Metal, punk, hardcore, rock |

---

## Arquitectura del sistema

El sistema sigue una arquitectura **Lambda/Kappa**: los datos fluyen desde los feeders hacia un event store persistente y simultáneamente a través de un broker de mensajería, permitiendo que la business-unit reconstruya su estado histórico al arrancar y reciba actualizaciones en tiempo real. La capa de presentación es una interfaz web servida por Javalin que conecta con Spotify para reproducción inmediata.

```mermaid
graph TD
    subgraph "Productores"
        WF[weather-feeder\nPython]
        LF[lastfm-feeder\nJava]
    end

    subgraph "Middleware EAI — ActiveMQ"
        B[ActiveMQ Broker\nport 61616 / 61613]
    end

    subgraph "Batch Layer — Data Lake"
        ESB[event-store-builder]
        ES[(eventstore\nNDJSON)]
    end

    subgraph "Speed & Serving Layer"
        BU[business-unit Java\nJavalin :8080]
        BUP[business-unit Python\nWeatherDatamart]
    end

    subgraph "Servicios externos"
        SP[Spotify API]
    end

    subgraph "Usuario"
        BR[Navegador web\n:8080]
    end

    WF -->|STOMP /topic/Weather| B
    LF -->|JMS topic/Track| B
    B -->|Durable Sub| ESB
    ESB -->|Append| ES
    B -->|JMS subscribe| BU
    B -->|STOMP subscribe| BUP
    ES -->|EventStoreReader| BU
    ES -->|EventStoreReader| BUP
    BR -->|HTTP REST| BU
    BU -->|OAuth2 + REST| SP
```

### Flujo de datos

1. **lastfm-feeder** consulta la API de Last.fm cada 6 horas y publica eventos `Track` (JSON) al topic `Track` de ActiveMQ.
2. **weather-feeder** consulta OpenWeatherMap cada hora y publica eventos `Weather` (JSON) al topic `Weather` vía STOMP.
3. **event-store-builder** está suscrito a ambos topics y persiste cada evento en ficheros NDJSON organizados por `topic/ss/fecha.events`.
4. **business-unit** al arrancar lee el event store completo para reconstruir el datamart SQLite y el estado de meteorología. Luego se suscribe en vivo a ambos topics para mantener los datos actualizados. Expone una interfaz web en `http://127.0.0.1:8080`.
5. El usuario abre el navegador, conecta Spotify (OAuth 2.0) y puede ver recomendaciones por clima y reproducirlas con un clic.

---

## Estructura del repositorio

```
music-weather-recommender/
├── lastfm-feeder/               # Sprint 1 — feeder de canciones (Java)
│   └── src/main/java/…/
│       ├── model/               # Tag, Track, TrackEvent
│       └── controller/          # Controller, LastFmApiFeeder, JmsTrackPublisher, serializers
├── event-store-builder/         # Sprint 2 — almacén de eventos (Java)
│   └── src/main/java/…/
│       └── controller/          # Controller, FileEventStore, JmsSubscriber
├── weather-feeder/              # Sprint 2 — feeder meteorológico (Python)
│   └── src/main/python/es/ulpgc/dacd/openweather/
│       ├── model/               # Location, Weather
│       └── controller/          # WeatherFeederController, OpenWeatherMapFeeder, publisher
├── business-unit/               # Sprint 3 — unidad de negocio (Java + Python)
│   ├── src/main/java/…/
│   │   ├── model/               # MoodMapping, Tag, Track
│   │   ├── controller/          # TrackDatamart, TrackRecommender, SpotifyExporter, …
│   │   └── view/                # WebServer (Javalin :8080)
│   ├── src/main/python/         # Python: datamart meteorológico (suscriptor en background)
│   │   └── controller/          # WeatherDatamart, EventStoreReader, subscriber
│   └── src/main/resources/web/  # index.html, app.js (Spotify Web Playback SDK)
├── eventstore/                  # Datos generados: eventos en NDJSON
│   ├── Track/lastfm-feeder/
│   └── Weather/OpenWeatherMap/
├── pom.xml                      # POM raíz (multi-módulo Maven)
├── run.sh                       # Script de arranque completo
├── .env.example                 # Plantilla de configuración
└── README.md
```

Cada módulo sigue la separación **model / controller / view** (view solo en business-unit):
- `model`: entidades de dominio sin dependencias externas.
- `controller`: lógica de negocio, acceso a datos, suscripciones al broker.
- `view`: interfaz web del usuario (Javalin + Spotify Web Playback SDK). Solo existe en business-unit Java.

---

## APIs utilizadas y justificación

### Last.fm API
- **Endpoint `geo.gettoptracks`**: top 300 canciones de España. Proporciona artistas y géneros con alta relevancia local.
- **Endpoint `chart.gettoptracks`**: top 300 canciones globales. Amplía el catálogo con tendencias internacionales.
- **Endpoint `track.gettoptags`**: etiquetas de género y estado de ánimo por canción, imprescindibles para la recomendación basada en mood.
- **Justificación**: Last.fm es la fuente de metadatos musicales etiquetados por comunidad más completa y accesible públicamente. Sus tags reflejan géneros y estados de ánimo con precisión validada por investigación académica (Çano & Morisio, 2017).

### OpenWeatherMap API
- **Endpoint `weather` (current)**: condición meteorológica actual por coordenadas geográficas.
- **Justificación**: API REST con tier gratuito generoso, cobertura global y campo `weather_main` con categorías discretas (Clear, Clouds, Rain, etc.) directamente mapeables a cuadrantes de ánimo.

### Spotify API
- **OAuth 2.0 Authorization Code Flow**: autenticación del usuario para obtener un token con scope `streaming` y `user-read-playback-state`.
- **Endpoint `GET /v1/search`**: búsqueda de canciones por nombre y artista para obtener el URI de Spotify.
- **Endpoint `PUT /v1/me/player`**: transferencia de reproducción al dispositivo del usuario.
- **Endpoint `PUT /v1/me/player/play`**: inicio de reproducción de una pista concreta.
- **Justificación**: Spotify Web Playback SDK permite reproducción en navegador sin aplicación nativa. La integración cierra el ciclo recomendación → reproducción directamente en la interfaz web.

---

## Estructura del datamart

El datamart principal es una base de datos SQLite con dos tablas:

```sql
CREATE TABLE tracks (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    name    TEXT NOT NULL,
    artist  TEXT NOT NULL,
    mbid    TEXT,
    url     TEXT,
    rank    INTEGER,
    ts      TEXT NOT NULL,   -- ISO-8601 UTC
    ss      TEXT NOT NULL,   -- sistema fuente
    UNIQUE(name, artist)
);

CREATE TABLE track_tags (
    track_id  INTEGER NOT NULL REFERENCES tracks(id) ON DELETE CASCADE,
    tag_name  TEXT NOT NULL,
    tag_count INTEGER          -- peso de la etiqueta (0-100)
);
```

La unicidad `(name, artist)` garantiza que las actualizaciones de rank (al reejecutar el feeder) sobreescriban el registro anterior mediante `ON CONFLICT DO UPDATE`.

El datamart meteorológico (Python/SQLite) mantiene la última lectura por ciudad:

```sql
CREATE TABLE weather (
    location_name TEXT PRIMARY KEY,
    country TEXT, lat REAL, lon REAL,
    temperature REAL, feels_like REAL, humidity INTEGER,
    weather_main TEXT, weather_description TEXT,
    wind_speed REAL, ts TEXT
);
```

### Muestra del event store — Track

```json
{"ts":"2026-05-18T18:40:45.546421Z","ss":"lastfm-feeder","name":"Billie Jean","artist":"Michael Jackson","mbid":"005fd94f-...","rank":3,"tags":[{"name":"pop","count":100},{"name":"80s","count":83},{"name":"dance","count":35}]}
```

### Muestra del event store — Weather

```json
{"ts":"2026-05-18T23:04:05.455972+00:00","ss":"OpenWeatherMap","location":{"name":"Las Palmas de Gran Canaria","lat":28.1,"lon":-15.4,"country":"ES"},"temperature":18.96,"humidity":68,"weather_main":"Clouds","weather_description":"scattered clouds"}
```

### Muestra del datamart

| name | artist | rank |
|---|---|---|
| Stateside + Zara Larsson | PinkPantheress | 0 |
| drop dead | Olivia Rodrigo | 1 |
| Billie Jean | Michael Jackson | 3 |

El datamart contiene actualmente **460 canciones** con **3070 etiquetas** de género/mood.

---

## Requisitos previos

| Herramienta | Versión mínima |
|---|---|
| Java (JDK) | 21 |
| Maven | 3.8 |
| Python | 3.10 |
| Apache ActiveMQ Classic | 5.x |
| stomp.py (Python) | cualquiera |
| requests (Python) | cualquiera |
| Cuenta Spotify Premium | (para reproducción web) |

Instalar dependencias Python:

```bash
pip3 install stomp.py requests
```

---

## Configuración

Copiar la plantilla y rellenar las claves:

```bash
cp .env.example .env
```

```env
LASTFM_API_KEY=tu_clave_lastfm
LASTFM_COUNTRY=spain
BROKER_URL=tcp://localhost:61616
EVENTSTORE_PATH=./eventstore
DATAMART_PATH=./datamart.db
OPENWEATHER_API_KEY=tu_clave_openweathermap
SPOTIFY_CLIENT_ID=tu_spotify_client_id
SPOTIFY_CLIENT_SECRET=tu_spotify_client_secret
```

> El fichero `.env` nunca debe commitearse. Está incluido en `.gitignore`.

### Configuración de la app Spotify

1. Acceder a [Spotify Developer Dashboard](https://developer.spotify.com/dashboard).
2. Crear una app y añadir como Redirect URI: `http://127.0.0.1:8080/callback`.
3. Copiar el Client ID y Client Secret al `.env`.

---

## Compilación

Desde la raíz del proyecto:

```bash
mvn package -DskipTests
```

Genera los JARs en `<módulo>/target/<módulo>-1.0-SNAPSHOT.jar`.

---

## Ejecución

### Opción A — Script automático (recomendado)

Arranca todos los módulos en el orden correcto:

```bash
./run.sh
```

El script compila, lanza `event-store-builder`, `lastfm-feeder` y `weather-feeder` en segundo plano, y finalmente inicia `business-unit` con el servidor web en `http://127.0.0.1:8080`. Al cerrar con `Ctrl+C`, los procesos de fondo se detienen automáticamente.

### Opción B — Manual (módulo a módulo)

**1. Asegurarse de que ActiveMQ está en marcha** (puerto 61616 JMS, 61613 STOMP).

**2. event-store-builder**
```bash
java -jar event-store-builder/target/event-store-builder-1.0-SNAPSHOT.jar \
  tcp://localhost:61616 ./eventstore
```

**3. lastfm-feeder**
```bash
java --enable-native-access=ALL-UNNAMED \
  -jar lastfm-feeder/target/lastfm-feeder-1.0-SNAPSHOT.jar \
  <LASTFM_API_KEY> spain tcp://localhost:61616
```

**4. weather-feeder**
```bash
cd weather-feeder
PYTHONPATH=src/main/python python3 -m es.ulpgc.dacd.openweather.main \
  <OPENWEATHER_API_KEY> 1
```
El segundo argumento es el intervalo de consulta en horas.

**5. business-unit**
```bash
java --enable-native-access=ALL-UNNAMED \
  -Dspotify.client.id=<CLIENT_ID> \
  -Dspotify.client.secret=<CLIENT_SECRET> \
  -jar business-unit/target/business-unit-1.0-SNAPSHOT.jar \
  tcp://localhost:61616 ./eventstore ./datamart.db
```

Si no se pasan las propiedades de Spotify, el sistema arranca sin reproducción (modo solo recomendaciones).

**6. business-unit Python (datamart meteorológico)**

Carga eventos históricos del event store, luego queda en background escuchando eventos de clima vía STOMP:

```bash
cd business-unit/src/main/python
python3 main.py <WEATHER_DB_PATH> <EVENTSTORE_PATH>
```

---

## Ejemplo de uso — Interfaz web

Abrir `http://127.0.0.1:8080` en el navegador:

1. Pulsar **Connect Spotify** para autorizar la cuenta (se abrirá el flujo OAuth). Tras la autorización se redirige de vuelta automáticamente.
2. Seleccionar una condición meteorológica (o usar la detección automática por ciudad).
3. El sistema muestra el ranking de canciones recomendadas para ese clima.
4. Pulsar **▶ Play** en cualquier canción para reproducirla en el dispositivo Spotify activo.

![Interfaz web](docs/screenshot.png)

### API REST (endpoints disponibles)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/` | Interfaz web principal |
| `GET` | `/api/token` | Estado de autenticación Spotify (`200` autenticado, `401` no autenticado) |
| `GET` | `/callback` | Callback OAuth 2.0 de Spotify |
| `GET` | `/api/search?name=…&artist=…` | Busca una canción en Spotify; devuelve `{"uri":"spotify:track:…"}` |
| `POST` | `/api/play` | Reproduce una canción; body `{"deviceId":"…","uri":"spotify:track:…"}` |

---

## Principios y patrones de diseño

### Principios SOLID

- **SRP (Single Responsibility)**: cada clase tiene una única responsabilidad. `TrackDatamart` solo gestiona persistencia; `TrackRecommender` solo ejecuta la lógica de recomendación; `WebServer` solo gestiona las rutas HTTP; `SpotifyExporter` solo gestiona la comunicación con la API de Spotify.
- **OCP (Open/Closed)**: la interfaz `EventHandler` permite añadir nuevos tipos de eventos sin modificar `EventStoreReader` ni el `Controller`.
- **DIP (Dependency Inversion)**: `Controller` depende de las abstracciones `LastFmFeeder` y `TrackSerializer`, no de sus implementaciones concretas. `WebServer` recibe `SpotifyExporter` por constructor y puede recibir `null` si Spotify no está configurado.

### Patrones de diseño

- **Strategy**: `TrackSerializer` es la estrategia; `JmsTrackPublisher` y `DatabaseTrackSerializer` son implementaciones intercambiables.
- **Observer**: el `MessageListener` de JMS implementa el patrón observador — el broker notifica a los suscriptores cuando llega un evento.
- **Repository**: `TrackDatamart` encapsula todo el acceso a SQLite y expone métodos de dominio (`save`, `findByTag`).
- **Template Method** (implícito): `LastFmFeeder` define el contrato `feed()` que `LastFmApiFeeder` implementa.
- **Proxy**: `WebServer` actúa como proxy entre el navegador y la API de Spotify. Todas las llamadas a `api.spotify.com` pasan por el servidor Java, que inyecta el token OAuth. Esto resuelve el bloqueo CORS de Safari y mantiene las credenciales fuera del cliente.

### Clean Code

- Nombres expresivos y sin abreviaciones.
- Logging mediante `java.util.logging.Logger` (nivel `WARNING` para errores de infraestructura).
- Sin comentarios redundantes; el código se autodocumenta.
- Métodos cortos con una sola responsabilidad.

---

## Diagrama de clases

El diagrama se divide en tres bloques siguiendo la separación lógica de la arquitectura Lambda implementada: los **productores** generan y publican eventos, el **event store builder** los persiste de forma inmutable conformando el Data Lake, y la **business unit** los consume y transforma en información útil para el usuario.

### Productores de datos
Muestra los dos módulos encargados de la extracción y publicación hacia el broker. El `weather-feeder` (Python) consulta la API de OpenWeatherMap, mientras que el `lastfm-feeder` (Java) obtiene canciones de Last.fm. Ambos siguen el patrón feeder → publisher → controller.

```mermaid
classDiagram
    class Location {
        +str name
        +float lat
        +float lon
        +str country
    }
    class Weather {
        +Location location
        +float temperature
        +int humidity
        +str weather_main
        +datetime captured_at
    }
    class WeatherFeeder {
        <<interface>>
        +feed()
    }
    class OpenWeatherMapFeeder {
        -str api_key
        -list locations
        +feed()
    }
    class WeatherPublisher {
        <<interface>>
        +publish(weather)
        +connect()
    }
    class ActiveMQWeatherPublisher {
        -str host
        -int port
        -str topic
        +publish(weather)
        +connect()
    }
    class WeatherFeederController {
        -feeder
        -publisher
        +start()
        +run()
    }
    class Tag {
        +String name
        +int count
    }
    class Track {
        +String name
        +String artist
        +int rank
        +List tags
    }
    class TrackEvent {
        +String ts
        +String ss
        +String name
        +String artist
    }
    class LastFmFeeder {
        <<interface>>
        +feed() List
    }
    class LastFmApiFeeder {
        -String apiKey
        +feed() List
    }
    class JmsTrackPublisher {
        -Session session
        +serialize(track)
    }
    class LastFmController {
        -LastFmFeeder feeder
        +start()
    }

    OpenWeatherMapFeeder --|> WeatherFeeder
    ActiveMQWeatherPublisher --|> WeatherPublisher
    LastFmApiFeeder --|> LastFmFeeder
    WeatherFeederController --> WeatherFeeder
    WeatherFeederController --> WeatherPublisher
    OpenWeatherMapFeeder --> Weather
    Weather --> Location
    TrackEvent --> Tag
    Track --> Tag
    LastFmController --> LastFmFeeder
    LastFmController --> JmsTrackPublisher
```

### Event Store Builder
Módulo responsable de la persistencia inmutable del sistema. Suscribe todos los topics de ActiveMQ y escribe cada evento en ficheros `.events` organizados por topic, fuente y fecha.

```mermaid
classDiagram
    class FileEventStore {
        -String basePath
        +save(topic, json)
    }
    class ESBJmsSubscriber {
        -String topicName
        -FileEventStore store
        +start(session)
    }
    class ESBController {
        -String brokerUrl
        +start()
    }

    ESBJmsSubscriber --> FileEventStore
    ESBController --> ESBJmsSubscriber
```

### Business Unit
Capa de consumo y presentación. Persiste los eventos en dos datamarts especializados — uno en Java para recomendaciones musicales y otro en Python para datos meteorológicos — y expone una interfaz web en Javalin con integración Spotify.

```mermaid
classDiagram
    class EventHandler {
        <<interface>>
        +handle(json)
    }
    class TrackEventHandler {
        -TrackDatamart datamart
        +handle(json)
    }
    class WeatherEventHandler {
        -WeatherState weatherState
        +handle(json)
    }
    class WeatherState {
        -Map latestByLocation
        +update(location, weatherMain)
        +getAll() Map
    }
    class TrackDatamart {
        -String dbPath
        +save(track)
        +findByTag(tag) List
    }
    class MoodMapping {
        +moodName(weatherMain) String
        +tagsFor(weatherMain) List
    }
    class TrackRecommender {
        -TrackDatamart datamart
        +recommend(weatherMain) List
    }
    class SpotifyExporter {
        -String clientId
        -String clientSecret
        -String accessToken
        +authorize(code)
        +searchTrack(name, artist) String
        +playTrack(deviceId, uri)
    }
    class WebServer {
        -TrackDatamart datamart
        -WeatherState weatherState
        -SpotifyExporter spotify
        +start()
    }
    class BUJmsSubscriber {
        -String topicName
        -EventHandler handler
        +start(session)
    }
    class BUController {
        -String brokerUrl
        +start()
    }
    class WeatherSubscriber {
        <<interface>>
        +start()
        +stop()
    }
    class ActiveMQWeatherSubscriber {
        -str host
        -str topic
        +start()
        +stop()
    }
    class WeatherDatamart {
        -str db_path
        +save(event)
        +get_all_latest() list
        +get_latest_by_location(name) dict
    }
    class PythonEventStoreReader {
        -str event_store_path
        +load(topic, handler)
    }

    TrackEventHandler --|> EventHandler
    WeatherEventHandler --|> EventHandler
    ActiveMQWeatherSubscriber --|> WeatherSubscriber
    TrackEventHandler --> TrackDatamart
    WeatherEventHandler --> WeatherState
    TrackRecommender --> TrackDatamart
    TrackRecommender --> MoodMapping
    BUJmsSubscriber --> EventHandler
    WebServer --> TrackRecommender
    WebServer --> WeatherState
    WebServer --> SpotifyExporter
    BUController --> BUJmsSubscriber
    ActiveMQWeatherSubscriber --> WeatherDatamart
    PythonEventStoreReader --> WeatherDatamart
```

---

## Localización de las ciudades monitorizadas

El `weather-feeder` consulta el tiempo actual de 8 ciudades de las Islas Canarias definidas en `weather-feeder/locations.json`:

- Las Palmas de Gran Canaria, Telde, Santa Cruz de Tenerife, Arrecife, Puerto del Rosario, Valverde, San Sebastián de La Gomera, Santa Cruz de La Palma.
