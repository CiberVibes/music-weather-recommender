package es.ulpgc.dacd.business.recommendation;

import es.ulpgc.dacd.business.datamart.TrackDatamart;
import es.ulpgc.dacd.business.model.Tag;
import es.ulpgc.dacd.business.model.Track;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackRecommenderTest {

    private Path tempDb;
    private TrackDatamart datamart;
    private TrackRecommender recommender;

    @BeforeEach
    void setUp() throws IOException {
        tempDb = Files.createTempFile("test-datamart", ".db");
        datamart = new TrackDatamart(tempDb.toString());
        recommender = new TrackRecommender(datamart);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempDb);
    }

    @Test
    void givenEmptyDatamart_whenRecommendForClear_thenReturnsEmptyList() {
        List<Track> result = recommender.recommend("Clear");
        assertTrue(result.isEmpty());
    }

    @Test
    void givenEmptyDatamart_whenRecommendForAnyWeather_thenReturnsEmptyList() {
        assertTrue(recommender.recommend("Thunderstorm").isEmpty());
        assertTrue(recommender.recommend("Rain").isEmpty());
        assertTrue(recommender.recommend("Clouds").isEmpty());
    }

    @Test
    void givenTrackWithHappyTag_whenRecommendForClear_thenReturnsTrack() {
        datamart.save(track("Happy Song", "Artist", 1, List.of(tag("happy", 100))));
        List<Track> result = recommender.recommend("Clear");
        assertFalse(result.isEmpty());
        assertEquals("Happy Song", result.get(0).getName());
    }

    @Test
    void givenTrackWithGenreTagOnly_whenRecommendForClear_thenFallsBackToGenreTag() {
        datamart.save(track("Pop Song", "Artist", 1, List.of(tag("pop", 80))));
        List<Track> result = recommender.recommend("Clear");
        assertFalse(result.isEmpty());
    }

    @Test
    void givenTrackWithAngryTag_whenRecommendForThunderstorm_thenReturnsTrack() {
        datamart.save(track("Metal Song", "Band", 1, List.of(tag("angry", 90))));
        List<Track> result = recommender.recommend("Thunderstorm");
        assertFalse(result.isEmpty());
        assertEquals("Metal Song", result.get(0).getName());
    }

    @Test
    void givenTrackWithMetalTag_whenRecommendForThunderstorm_thenFallsBackToGenreTag() {
        datamart.save(track("Metal Song", "Band", 1, List.of(tag("metal", 90))));
        List<Track> result = recommender.recommend("Thunderstorm");
        assertFalse(result.isEmpty());
    }

    @Test
    void givenTrackWithSadTag_whenRecommendForRain_thenReturnsTrack() {
        datamart.save(track("Sad Song", "Artist", 1, List.of(tag("sad", 90))));
        List<Track> result = recommender.recommend("Rain");
        assertFalse(result.isEmpty());
        assertEquals("Sad Song", result.get(0).getName());
    }

    @Test
    void givenTrackWithSadTag_whenRecommendForDrizzle_thenReturnsSameResultAsRain() {
        datamart.save(track("Sad Song", "Artist", 1, List.of(tag("sad", 90))));
        assertEquals(recommender.recommend("Rain").size(), recommender.recommend("Drizzle").size());
    }

    @Test
    void givenTrackWithSadTag_whenRecommendForSnow_thenReturnsSameResultAsRain() {
        datamart.save(track("Sad Song", "Artist", 1, List.of(tag("sad", 90))));
        assertEquals(recommender.recommend("Rain").size(), recommender.recommend("Snow").size());
    }

    @Test
    void givenTrackWithRelaxedTag_whenRecommendForClouds_thenReturnsTrack() {
        datamart.save(track("Chill Song", "Artist", 1, List.of(tag("relaxed", 90))));
        List<Track> result = recommender.recommend("Clouds");
        assertFalse(result.isEmpty());
    }

    @Test
    void givenTrackWithAmbientTag_whenRecommendForClouds_thenFallsBackToGenreTag() {
        datamart.save(track("Ambient Song", "Artist", 1, List.of(tag("ambient", 80))));
        List<Track> result = recommender.recommend("Clouds");
        assertFalse(result.isEmpty());
    }

    @Test
    void givenMoodTagAndGenreTagBothPresent_whenRecommend_thenMoodTagWinsOverGenreTag() {
        datamart.save(track("Happy Song", "Artist A", 1, List.of(tag("happy", 100))));
        datamart.save(track("Pop Song", "Artist B", 2, List.of(tag("pop", 80))));
        List<Track> result = recommender.recommend("Clear");
        assertEquals("Happy Song", result.get(0).getName());
    }

    @Test
    void givenMultipleTracksMatchingFirstTag_whenRecommend_thenAllAreReturned() {
        datamart.save(track("Song A", "Artist A", 1, List.of(tag("happy", 100))));
        datamart.save(track("Song B", "Artist B", 2, List.of(tag("happy", 80))));
        List<Track> result = recommender.recommend("Clear");
        assertEquals(2, result.size());
    }

    @Test
    void givenUnknownWeatherCondition_whenRecommend_thenUsesDefaultHappyTags() {
        datamart.save(track("Song", "Artist", 1, List.of(tag("pop", 70))));
        List<Track> result = recommender.recommend("Tornado");
        assertFalse(result.isEmpty());
    }

    @Test
    void givenTrackWithPopTag_whenRecommendForUnknownAndClear_thenBothReturnSameCount() {
        datamart.save(track("Song", "Artist", 1, List.of(tag("pop", 80))));
        List<Track> clearResult = recommender.recommend("Clear");
        List<Track> tornadoResult = recommender.recommend("Tornado");
        assertEquals(clearResult.size(), tornadoResult.size());
    }

    private Track track(String name, String artist, int rank, List<Tag> tags) {
        return new Track(name, artist, "mbid-123", "http://url", rank, "2024-01-01", "lastfm-feeder", tags);
    }

    private Tag tag(String name, int count) {
        return new Tag(name, count);
    }
}
