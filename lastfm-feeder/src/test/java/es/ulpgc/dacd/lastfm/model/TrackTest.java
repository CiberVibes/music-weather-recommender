package es.ulpgc.dacd.lastfm.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackTest {

    @Test
    void givenTrackData_whenCreated_thenGettersReturnExpectedValues() {
        Instant now = Instant.now();
        Tag tag = new Tag("rock", 100);
        Track track = new Track("Bohemian Rhapsody", "Queen", "abc123", "http://last.fm/track", 1, now, List.of(tag));

        assertEquals("Bohemian Rhapsody", track.getName());
        assertEquals("Queen", track.getArtist());
        assertEquals("abc123", track.getMbid());
        assertEquals("http://last.fm/track", track.getUrl());
        assertEquals(1, track.getRank());
        assertEquals(now, track.getCapturedAt());
        assertEquals(1, track.getTags().size());
        assertEquals("rock", track.getTags().get(0).getName());
    }

    @Test
    void givenTrackWithNoTags_whenGetTags_thenReturnsEmptyList() {
        Track track = new Track("Song", "Artist", "", "", 1, Instant.now(), List.of());

        assertTrue(track.getTags().isEmpty());
    }
}
