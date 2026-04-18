package es.ulpgc.dacd.lastfm.model;

import java.time.Instant;
import java.util.List;

public class Track {
    private final String name;
    private final String artist;
    private final String mbid;
    private final String url;
    private final int rank;
    private final Instant capturedAt;
    private final List<Tag> tags;

    public Track(String name, String artist, String mbid, String url, int rank, Instant capturedAt, List<Tag> tags) {
        this.name = name;
        this.artist = artist;
        this.mbid = mbid;
        this.url = url;
        this.rank = rank;
        this.capturedAt = capturedAt;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getMbid() {
        return mbid;
    }

    public String getUrl() {
        return url;
    }

    public int getRank() {
        return rank;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public List<Tag> getTags() {
        return tags;
    }
}
