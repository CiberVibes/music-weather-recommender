package es.ulpgc.dacd.business.controller;

import es.ulpgc.dacd.business.model.Track;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackEventHandlerTest {

    private Path tempDb;
    private TrackDatamart datamart;
    private TrackEventHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        tempDb = Files.createTempFile("test-datamart", ".db");
        datamart = new TrackDatamart(tempDb.toString());
        handler = new TrackEventHandler(datamart, new TrackRecommender(datamart), new WeatherState());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempDb);
    }

    @Test
    void givenValidTrackJson_whenHandle_thenTrackIsSavedToDatamart() {
        handler.handle(trackJson("Stairway to Heaven", "Led Zeppelin", "abc123", 1, "[{\"name\":\"rock\",\"count\":100}]"));
        List<Track> result = datamart.findByTag("rock");
        assertEquals(1, result.size());
        assertEquals("Stairway to Heaven", result.get(0).getName());
    }

    @Test
    void givenValidTrackJson_whenHandle_thenArtistIsCorrectlyParsed() {
        handler.handle(trackJson("Song", "The Beatles", "abc", 1, "[{\"name\":\"british invasion\",\"count\":90}]"));
        List<Track> result = datamart.findByTag("british invasion");
        assertEquals("The Beatles", result.get(0).getArtist());
    }

    @Test
    void givenValidTrackJson_whenHandle_thenRankIsCorrectlyParsed() {
        handler.handle(trackJson("Song", "Artist", "abc", 42, "[{\"name\":\"pop\",\"count\":50}]"));
        List<Track> result = datamart.findByTag("pop");
        assertEquals(42, result.get(0).getRank());
    }

    @Test
    void givenTrackJsonWithNullMbid_whenHandle_thenTrackIsSaved() {
        String json = """
                {"name":"Song","artist":"Artist","mbid":null,
                 "url":"http://last.fm","rank":1,
                 "ts":"2024-01-01T12:00:00Z","ss":"lastfm-feeder",
                 "tags":[{"name":"pop","count":50}]}
                """;
        handler.handle(json);
        assertFalse(datamart.findByTag("pop").isEmpty());
    }

    @Test
    void givenTrackJsonWithMissingMbidField_whenHandle_thenTrackIsSaved() {
        String json = """
                {"name":"Song","artist":"Artist",
                 "url":"http://last.fm","rank":1,
                 "ts":"2024-01-01T12:00:00Z","ss":"lastfm-feeder",
                 "tags":[{"name":"pop","count":50}]}
                """;
        handler.handle(json);
        assertFalse(datamart.findByTag("pop").isEmpty());
    }

    @Test
    void givenTrackJsonWithEmptyTagsArray_whenHandle_thenTrackIsSavedWithNoTags() {
        handler.handle(trackJson("Song", "Artist", "abc", 1, "[]"));
        assertTrue(datamart.findByTag("rock").isEmpty());
    }

    @Test
    void givenTrackJsonWithMultipleTags_whenHandle_thenAllTagsAreSaved() {
        handler.handle(trackJson("Song", "Artist", "abc", 1, "[{\"name\":\"pop\",\"count\":80},{\"name\":\"dance\",\"count\":60}]"));
        assertFalse(datamart.findByTag("pop").isEmpty());
        assertFalse(datamart.findByTag("dance").isEmpty());
    }

    @Test
    void givenInvalidJson_whenHandle_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> handler.handle("not valid json {{{"));
    }

    @Test
    void givenEmptyJsonObject_whenHandle_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> handler.handle("{}"));
    }

    @Test
    void givenTwoEventsForSameTrack_whenHandle_thenOnlyOneTrackExistsInDatamart() {
        String json = trackJson("Song", "Artist", "abc", 1, "[{\"name\":\"pop\",\"count\":50}]");
        handler.handle(json);
        handler.handle(json);
        List<Track> result = datamart.findByTag("pop");
        assertEquals(1, result.size());
    }

    private String trackJson(String name, String artist, String mbid, int rank, String tagsJson) {
        return String.format(
                "{\"name\":\"%s\",\"artist\":\"%s\",\"mbid\":\"%s\"," +
                "\"url\":\"http://last.fm\",\"rank\":%d," +
                "\"ts\":\"2024-01-01T12:00:00Z\",\"ss\":\"lastfm-feeder\"," +
                "\"tags\":%s}",
                name, artist, mbid, rank, tagsJson);
    }
}
