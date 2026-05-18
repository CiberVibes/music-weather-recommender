#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

if [ ! -f "$ENV_FILE" ]; then
  echo "Error: .env file not found. Copy .env.example to .env and fill in your values."
  exit 1
fi

source "$ENV_FILE"

BROKER_URL=${BROKER_URL:-tcp://localhost:61616}
EVENTSTORE_PATH=${EVENTSTORE_PATH:-$SCRIPT_DIR/eventstore}
DATAMART_PATH=${DATAMART_PATH:-$SCRIPT_DIR/datamart.db}

if command -v mvn &>/dev/null; then
  MVN=mvn
elif [ -f "/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" ]; then
  MVN="/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn"
else
  echo "Error: Maven not found. Install Maven or open the project in IntelliJ first."
  exit 1
fi

echo "Building project..."
"$MVN" package -f "$SCRIPT_DIR/pom.xml" -DskipTests -q
echo "Build complete."

pkill -f "event-store-builder.*jar" 2>/dev/null || true
pkill -f "lastfm-feeder.*jar" 2>/dev/null || true
pkill -f "business-unit.*jar" 2>/dev/null || true
sleep 1

echo "Starting event-store-builder..."
java -jar "$SCRIPT_DIR/event-store-builder/target/event-store-builder-1.0-SNAPSHOT.jar" \
  "$BROKER_URL" "$EVENTSTORE_PATH" &
ESB_PID=$!

echo "Starting lastfm-feeder..."
java -jar "$SCRIPT_DIR/lastfm-feeder/target/lastfm-feeder-1.0-SNAPSHOT.jar" \
  "$LASTFM_API_KEY" "$LASTFM_COUNTRY" "$BROKER_URL" &
FEEDER_PID=$!

trap "echo 'Stopping...'; kill $ESB_PID $FEEDER_PID 2>/dev/null; exit 0" INT TERM

echo "Starting business-unit..."
java -jar "$SCRIPT_DIR/business-unit/target/business-unit-1.0-SNAPSHOT.jar" \
  "$BROKER_URL" "$EVENTSTORE_PATH" "$DATAMART_PATH"

kill $ESB_PID $FEEDER_PID 2>/dev/null
