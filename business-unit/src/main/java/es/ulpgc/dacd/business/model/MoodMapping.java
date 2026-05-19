package es.ulpgc.dacd.business.model;

import java.util.List;

public class MoodMapping {

    public static String moodName(String weatherMain) {
        return switch (weatherMain.toLowerCase()) {
            case "clear"                        -> "Happy (Q1)";
            case "thunderstorm"                 -> "Angry (Q2)";
            case "rain", "drizzle", "snow",
                 "fog", "mist", "smoke", "haze" -> "Sad (Q3)";
            case "clouds"                       -> "Relaxed (Q4)";
            default                             -> "Happy (Q1)";
        };
    }

    public static List<String> tagsFor(String weatherMain) {
        return switch (weatherMain.toLowerCase()) {
            case "clear" -> List.of(
                    "happy", "happiness", "joyous", "cheerful", "fun", "bright", "exciting",
                    "pop", "dance", "pop punk", "electropop", "disco", "funk", "party");
            case "thunderstorm" -> List.of(
                    "angry", "aggressive", "fierce", "tense", "rebellious", "fiery",
                    "metal", "heavy metal", "punk", "hardcore", "rock", "punk rock");
            case "rain", "drizzle", "snow", "fog", "mist", "smoke", "haze" -> List.of(
                    "sad", "bittersweet", "gloomy", "sorrow", "melancholic", "depressing",
                    "emo", "post-rock", "acoustic", "indie folk", "blues", "slowcore");
            case "clouds" -> List.of(
                    "relaxed", "calm", "peaceful", "mellow", "soothing", "gentle",
                    "ambient", "chillout", "indie pop", "soft rock", "lo-fi", "singer-songwriter");
            default -> List.of("pop", "rock", "indie");
        };
    }
}
