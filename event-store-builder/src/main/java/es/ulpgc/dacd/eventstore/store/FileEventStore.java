package es.ulpgc.dacd.eventstore.store;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class FileEventStore {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private final String basePath;

    public FileEventStore(String basePath) {
        this.basePath = basePath;
    }

    public void save(String topic, String json) {
        JsonObject event = JsonParser.parseString(json).getAsJsonObject();
        String ss = event.get("ss").getAsString();
        String ts = event.get("ts").getAsString();
        String date = DATE_FORMAT.format(Instant.parse(ts));
        Path filePath = Path.of(basePath, topic, ss, date + ".events");
        writeEvent(filePath, json);
    }

    private void writeEvent(Path filePath, String json) {
        try {
            Files.createDirectories(filePath.getParent());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), true))) {
                writer.write(json);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
