package es.ulpgc.dacd.lastfm.model;

import java.util.List;

public class TrackEvent {

    private final String ts;
    private final String ss;
    private final String name;
    private final String artist;
    private final String mbid;
    private final String url;
    private final int rank;
    private final List<Tag> tags;

    public TrackEvent(String ss, Track track) {
        this.ts = track.getCapturedAt().toString();
        this.ss = ss;
        this.name = track.getName();
        this.artist = track.getArtist();
        this.mbid = track.getMbid();
        this.url = track.getUrl();
        this.rank = track.getRank();
        this.tags = track.getTags();
    }

    public String getTs() {
        return ts;
    }

    public String getSs() {
        return ss;
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

    public List<Tag> getTags() {
        return tags;
    }
}
