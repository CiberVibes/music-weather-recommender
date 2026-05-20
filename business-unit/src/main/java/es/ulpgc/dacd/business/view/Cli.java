package es.ulpgc.dacd.business.view;

import es.ulpgc.dacd.business.controller.SpotifyExporter;
import es.ulpgc.dacd.business.controller.TrackDatamart;
import es.ulpgc.dacd.business.controller.WeatherState;
import es.ulpgc.dacd.business.model.MoodMapping;
import es.ulpgc.dacd.business.model.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Cli {

    private final TrackDatamart datamart;
    private final WeatherState weatherState;
    private final SpotifyExporter spotify;
    private final Scanner scanner;

    public Cli(TrackDatamart datamart, WeatherState weatherState, SpotifyExporter spotify) {
        this.datamart = datamart;
        this.weatherState = weatherState;
        this.spotify = spotify;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printHeader();
        while (true) {
            Map.Entry<String, String> selection = pickLocation();
            if (selection == null) break;
            List<Track> tracks = showRecommendations(selection);
            handleExportPrompt(tracks, selection);
        }
        System.out.println("\nGoodbye!");
    }

    private void printHeader() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║     Music Weather Recommender        ║");
        System.out.println("╚══════════════════════════════════════╝");
    }

    private Map.Entry<String, String> pickLocation() {
        List<Map.Entry<String, String>> locations = new ArrayList<>(weatherState.getAll().entrySet());

        if (locations.isEmpty()) {
            System.out.println("\nNo live weather data yet. Waiting for the weather feeder...");
            System.out.println("  0. Exit");
            System.out.print("\nOption: ");
            String input = scanner.nextLine().trim();
            if ("0".equals(input)) return null;
            return pickLocation();
        }

        System.out.println("\nSelect your location:");
        for (int i = 0; i < locations.size(); i++) {
            Map.Entry<String, String> e = locations.get(i);
            String mood = MoodMapping.moodName(e.getValue());
            System.out.printf("  %d. %-30s%-14s→  %s%n", i + 1, e.getKey(), e.getValue(), mood);
        }
        System.out.println("  0. Exit");
        System.out.print("\nOption: ");

        String input = scanner.nextLine().trim();
        try {
            int choice = Integer.parseInt(input);
            if (choice == 0) return null;
            if (choice >= 1 && choice <= locations.size()) return locations.get(choice - 1);
        } catch (NumberFormatException ignored) {}

        System.out.println("Invalid option. Enter a number between 0 and " + locations.size() + ".");
        return pickLocation();
    }

    private List<Track> showRecommendations(Map.Entry<String, String> selection) {
        String location = selection.getKey();
        String condition = selection.getValue();
        String mood = MoodMapping.moodName(condition);
        List<Track> tracks = datamart.findRecommendations(location);

        System.out.println("\n" + location + " — " + condition + "  →  Mood: " + mood);
        System.out.println("─".repeat(50));

        if (tracks.isEmpty()) {
            System.out.println("No recommendations yet for this location. Try again in a moment.");
            return tracks;
        }

        int shown = Math.min(10, tracks.size());
        for (int i = 0; i < shown; i++) {
            Track t = tracks.get(i);
            System.out.printf("  %2d. %s — %s%n", i + 1, t.getName(), t.getArtist());
        }
        System.out.printf("%n  Showing %d track(s).%n", shown);
        return tracks;
    }

    private void handleExportPrompt(List<Track> tracks, Map.Entry<String, String> selection) {
        System.out.print("\nPress Enter to go back...");
        scanner.nextLine();
    }
}
