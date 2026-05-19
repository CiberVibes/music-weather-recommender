import logging
from pathlib import Path

logger = logging.getLogger(__name__)


class EventStoreReader:
    def __init__(self, event_store_path: str):
        self.event_store_path = Path(event_store_path)

    def load(self, topic: str, handler) -> None:
        topic_path = self.event_store_path / topic
        if not topic_path.exists():
            logger.warning(f"Path not found: {topic_path}")
            return

        for file_path in sorted(topic_path.rglob('*.events')):
            self._load_file(file_path, handler)

    def _load_file(self, file_path: Path, handler) -> None:
        try:
            for line in file_path.read_text().splitlines():
                if line.strip():
                    handler(line.strip())
        except Exception as e:
            logger.error(f"Failed to read file {file_path}: {e}")
