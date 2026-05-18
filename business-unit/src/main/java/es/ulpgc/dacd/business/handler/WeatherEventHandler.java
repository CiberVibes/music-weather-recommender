package es.ulpgc.dacd.business.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.logging.Logger;

public class WeatherEventHandler implements EventHandler {

    private static final Logger logger = Logger.getLogger(WeatherEventHandler.class.getName());

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
            logger.severe("Failed to handle weather event: " + e.getMessage());
        }
    }
}
