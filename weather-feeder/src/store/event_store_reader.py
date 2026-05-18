import os


class EventStoreReader:
    def __init__(self, event_store_path: str):
        self.event_store_path = event_store_path

    def load(self, topic: str, handler) -> None:
        topic_path = os.path.join(self.event_store_path, topic)
        if not os.path.exists(topic_path):
            print(f"[event-store-reader] Path not found: {topic_path}")
            return

        event_files = []
        for root, _, files in os.walk(topic_path):
            for file in files:
                if file.endswith('.events'):
                    event_files.append(os.path.join(root, file))

        for file_path in sorted(event_files):
            self._load_file(file_path, handler)

    def _load_file(self, file_path: str, handler) -> None:
        try:
            with open(file_path, 'r') as f:
                for line in f:
                    line = line.strip()
                    if line:
                        handler(line)
        except Exception as e:
            print(f"[event-store-reader] Failed to read file {file_path}: {e}")
