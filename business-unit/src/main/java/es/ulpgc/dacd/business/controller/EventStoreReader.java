package es.ulpgc.dacd.business.controller;

import es.ulpgc.dacd.business.handler.EventHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class EventStoreReader {

    private static final Logger logger = Logger.getLogger(EventStoreReader.class.getName());

    private final String eventStorePath;

    public EventStoreReader(String eventStorePath) {
        this.eventStorePath = eventStorePath;
    }

    public void load(String topic, EventHandler handler) {
        Path topicPath = Path.of(eventStorePath, topic);
        if (!Files.exists(topicPath)) return;
        try {
            Files.walk(topicPath)
                    .filter(p -> p.toString().endsWith(".events"))
                    .sorted()
                    .forEach(file -> loadFile(file, handler));
        } catch (IOException e) {
            logger.severe("Failed to read event store: " + e.getMessage());
        }
    }

    private void loadFile(Path file, EventHandler handler) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) handler.handle(line);
            }
        } catch (IOException e) {
            logger.severe("Failed to read file " + file + ": " + e.getMessage());
        }
    }
}
