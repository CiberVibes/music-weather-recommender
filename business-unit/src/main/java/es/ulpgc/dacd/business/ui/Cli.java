package es.ulpgc.dacd.business.ui;

import es.ulpgc.dacd.business.model.Track;
import es.ulpgc.dacd.business.recommendation.MoodMapper;
import es.ulpgc.dacd.business.recommendation.TrackRecommender;

import java.util.List;
import java.util.Scanner;

public class Cli {

    private static final List<String> VALID_CONDITIONS =
            List.of("Clear", "Clouds", "Rain", "Drizzle", "Snow", "Thunderstorm", "Fog", "Mist");

    private final TrackRecommender recommender;
    private final Scanner scanner;

    public Cli(TrackRecommender recommender) {
        this.recommender = recommender;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== Music Weather Recommender ===");
        while (true) {
            String condition = askWeatherCondition();
            if (condition.equalsIgnoreCase("exit")) break;
            showRecommendations(condition);
        }
        System.out.println("Goodbye!");
    }

    private String askWeatherCondition() {
        System.out.println("\nAvailable conditions: " + String.join(", ", VALID_CONDITIONS));
        System.out.print("Enter weather condition (or 'exit' to quit): ");
        return scanner.nextLine().trim();
    }

    private void showRecommendations(String condition) {
        String mood = MoodMapper.moodName(condition);
        List<Track> tracks = recommender.recommend(condition);

        System.out.println("\nWeather: " + condition + " → Mood: " + mood);
        System.out.println("─".repeat(50));

        if (tracks.isEmpty()) {
            System.out.println("No tracks found for this mood. Try again later.");
            return;
        }

        System.out.println("Top recommended tracks:");
        for (int i = 0; i < Math.min(10, tracks.size()); i++) {
            Track t = tracks.get(i);
            System.out.printf("  %2d. %s — %s%n", i + 1, t.getName(), t.getArtist());
        }
    }
}
