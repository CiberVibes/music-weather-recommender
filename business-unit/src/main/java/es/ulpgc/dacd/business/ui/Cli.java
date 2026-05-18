package es.ulpgc.dacd.business.ui;

import es.ulpgc.dacd.business.handler.WeatherState;
import es.ulpgc.dacd.business.model.Track;
import es.ulpgc.dacd.business.recommendation.MoodMapper;
import es.ulpgc.dacd.business.recommendation.TrackRecommender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Cli {

    private static final List<String> CONDITIONS =
            List.of("Clear", "Clouds", "Rain", "Drizzle", "Snow", "Thunderstorm", "Fog", "Mist");

    private final TrackRecommender recommender;
    private final WeatherState weatherState;
    private final Scanner scanner;

    public Cli(TrackRecommender recommender, WeatherState weatherState) {
        this.recommender = recommender;
        this.weatherState = weatherState;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printHeader();
        while (true) {
            String condition = pickCondition();
            if (condition == null) break;
            showRecommendations(condition);
            waitForEnter();
        }
        System.out.println("\nGoodbye!");
    }

    private void printHeader() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║     Music Weather Recommender        ║");
        System.out.println("╚══════════════════════════════════════╝");
    }

    private String pickCondition() {
        List<Map.Entry<String, String>> liveEntries = new ArrayList<>(weatherState.getAll().entrySet());

        System.out.println("\nSelect a weather condition:");
        for (int i = 0; i < CONDITIONS.size(); i++) {
            System.out.printf("  %d. %-14s→  %s%n", i + 1, CONDITIONS.get(i), MoodMapper.moodName(CONDITIONS.get(i)));
        }
        if (!liveEntries.isEmpty()) {
            System.out.println("\n  Live weather:");
            for (int i = 0; i < liveEntries.size(); i++) {
                Map.Entry<String, String> e = liveEntries.get(i);
                System.out.printf("  %d. %-38s→  %s%n",
                        CONDITIONS.size() + i + 1, e.getKey(), e.getValue());
            }
        }
        System.out.println("  0. Exit");
        System.out.print("\nOption: ");

        String input = scanner.nextLine().trim();
        try {
            int choice = Integer.parseInt(input);
            if (choice == 0) return null;
            if (choice >= 1 && choice <= CONDITIONS.size()) return CONDITIONS.get(choice - 1);
            int liveIndex = choice - CONDITIONS.size() - 1;
            if (liveIndex >= 0 && liveIndex < liveEntries.size()) return liveEntries.get(liveIndex).getValue();
        } catch (NumberFormatException ignored) {}

        int max = CONDITIONS.size() + liveEntries.size();
        System.out.println("Invalid option. Enter a number between 0 and " + max + ".");
        return pickCondition();
    }

    private void showRecommendations(String condition) {
        String mood = MoodMapper.moodName(condition);
        List<Track> tracks = recommender.recommend(condition);

        System.out.println("\nWeather: " + condition + "  →  Mood: " + mood);
        System.out.println("─".repeat(50));

        if (tracks.isEmpty()) {
            System.out.println("No tracks found for this mood. Try again later.");
            return;
        }

        int shown = Math.min(10, tracks.size());
        for (int i = 0; i < shown; i++) {
            Track t = tracks.get(i);
            System.out.printf("  %2d. %s — %s%n", i + 1, t.getName(), t.getArtist());
        }
        System.out.printf("%n  Showing %d track(s).%n", shown);
    }

    private void waitForEnter() {
        System.out.print("\nPress Enter to go back...");
        scanner.nextLine();
    }
}
