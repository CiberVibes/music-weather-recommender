package es.ulpgc.dacd.business.controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeatherState {

    private final ConcurrentHashMap<String, String> latestByLocation = new ConcurrentHashMap<>();

    public void update(String location, String weatherMain) {
        latestByLocation.put(location, weatherMain);
    }

    public Map<String, String> getAll() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(latestByLocation));
    }

    public boolean hasData() {
        return !latestByLocation.isEmpty();
    }
}
