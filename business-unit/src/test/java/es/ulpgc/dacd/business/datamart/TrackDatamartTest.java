package es.ulpgc.dacd.business.datamart;

import es.ulpgc.dacd.business.controller.TrackDatamart;
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

class TrackDatamartTest {

    private Path tempDb;
    private TrackDatamart datamart;

    @BeforeEach
    void setUp() throws IOException {
        tempDb = Files.createTempFile("test-datamart", ".db");
        datamart = new TrackDatamart(tempDb.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempDb);
    }

    @Test
    void givenTrackWithTag_whenSave_thenFindByTagReturnsTrack() {
        datamart.save(track("Bohemian Rhapsody", "Queen", 1, List.of(tag("rock", 100))));
        List<Track> result = datamart.findByTag("rock");
        assertEquals(1, result.size());
        assertEquals("Bohemian Rhapsody", result.get(0).getName());
    }

    @Test
    void givenTrackWithTag_whenFindByTagCaseInsensitive_thenTrackIsFound() {
        datamart.save(track("Song", "Artist", 1, List.of(tag("Pop", 50))));
        List<Track> result = datamart.findByTag("pop");
        assertFalse(result.isEmpty());
        assertEquals("Song", result.get(0).getName());
    }

    @Test
    void givenNoMatchingTag_whenFindByTag_thenReturnsEmptyList() {
        datamart.save(track("Song", "Artist", 1, List.of(tag("rock", 50))));
        List<Track> result = datamart.findByTag("jazz");
        assertTrue(result.isEmpty());
    }

    @Test
    void givenMultipleTracksWithSameTag_whenFindByTag_thenAllAreReturned() {
        datamart.save(track("Song A", "Artist A", 1, List.of(tag("pop", 80))));
        datamart.save(track("Song B", "Artist B", 2, List.of(tag("pop", 70))));
        List<Track> result = datamart.findByTag("pop");
        assertEquals(2, result.size());
    }

    @Test
    void givenTracksWithDifferentRanks_whenFindByTag_thenReturnedOrderedByRankAscending() {
        datamart.save(track("Song A", "Artist A", 3, List.of(tag("pop", 80))));
        datamart.save(track("Song B", "Artist B", 1, List.of(tag("pop", 80))));
        datamart.save(track("Song C", "Artist C", 2, List.of(tag("pop", 80))));
        List<Track> result = datamart.findByTag("pop");
        assertEquals("Song B", result.get(0).getName());
        assertEquals("Song C", result.get(1).getName());
        assertEquals("Song A", result.get(2).getName());
    }

    @Test
    void givenExistingTrack_whenSavedWithUpdatedRank_thenRankIsUpdated() {
        datamart.save(track("Song", "Artist", 5, List.of(tag("pop", 50))));
        datamart.save(track("Song", "Artist", 1, List.of(tag("pop", 50))));
        List<Track> result = datamart.findByTag("pop");
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getRank());
    }

    @Test
    void givenExistingTrack_whenSavedAgain_thenNoDuplicatesAreCreated() {
        datamart.save(track("Song", "Artist", 1, List.of(tag("pop", 50))));
        datamart.save(track("Song", "Artist", 2, List.of(tag("pop", 50))));
        List<Track> result = datamart.findByTag("pop");
        assertEquals(1, result.size());
    }

    @Test
    void givenTrackResavedWithDifferentTags_whenFindByOldTag_thenReturnsEmpty() {
        datamart.save(track("Song", "Artist", 1, List.of(tag("pop", 50))));
        datamart.save(track("Song", "Artist", 1, List.of(tag("rock", 50))));
        List<Track> result = datamart.findByTag("pop");
        assertTrue(result.isEmpty());
    }

    @Test
    void givenTrackResavedWithDifferentTags_whenFindByNewTag_thenReturnsTrack() {
        datamart.save(track("Song", "Artist", 1, List.of(tag("pop", 50))));
        datamart.save(track("Song", "Artist", 1, List.of(tag("rock", 50))));
        List<Track> result = datamart.findByTag("rock");
        assertEquals(1, result.size());
    }

    @Test
    void givenTrackWithNullMbid_whenSave_thenTrackIsFoundByTag() {
        Track nullMbid = new Track("Song", "Artist", null, "http://url", 1, "2024-01-01", "lastfm-feeder", List.of(tag("pop", 50)));
        datamart.save(nullMbid);
        List<Track> result = datamart.findByTag("pop");
        assertEquals(1, result.size());
    }

    @Test
    void givenTrackWithNoTags_whenSave_thenFindByTagReturnsEmpty() {
        datamart.save(track("Song", "Artist", 1, List.of()));
        List<Track> result = datamart.findByTag("pop");
        assertTrue(result.isEmpty());
    }

    @Test
    void givenTrackWithMultipleTags_whenFindByEachTag_thenTrackIsFound() {
        datamart.save(track("Song", "Artist", 1, List.of(tag("pop", 80), tag("dance", 60))));
        assertFalse(datamart.findByTag("pop").isEmpty());
        assertFalse(datamart.findByTag("dance").isEmpty());
    }

    @Test
    void givenEmptyDatamart_whenFindByTag_thenReturnsEmptyList() {
        List<Track> result = datamart.findByTag("pop");
        assertTrue(result.isEmpty());
    }

    private Track track(String name, String artist, int rank, List<Tag> tags) {
        return new Track(name, artist, "mbid-123", "http://url", rank, "2024-01-01", "lastfm-feeder", tags);
    }

    private Tag tag(String name, int count) {
        return new Tag(name, count);
    }
}
