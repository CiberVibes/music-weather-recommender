package es.ulpgc.dacd.business.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WeatherEventHandler implements EventHandler {

    private final WeatherState weatherState;

    public WeatherEventHandler(WeatherState weatherState) {
        this.weatherState = weatherState;
    }

    @Override
    public void handle(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String location = obj.getAsJsonObject("location").get("name").getAsString();
            String weatherMain = obj.get("weather_main").getAsString();
            weatherState.update(location, weatherMain);
        } catch (Exception e) {
            System.err.println("[business-unit] Failed to handle weather event: " + e.getMessage());
        }
    }
}
