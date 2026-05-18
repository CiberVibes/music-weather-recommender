package es.ulpgc.dacd.business.model;

import java.util.List;

public class Track {
    private final String name;
    private final String artist;
    private final String mbid;
    private final String url;
    private final int rank;
    private final String ts;
    private final String ss;
    private final List<Tag> tags;

    public Track(String name, String artist, String mbid, String url, int rank, String ts, String ss, List<Tag> tags) {
        this.name = name;
        this.artist = artist;
        this.mbid = mbid;
        this.url = url;
        this.rank = rank;
        this.ts = ts;
        this.ss = ss;
        this.tags = tags;
    }

    public String getName() { return name; }
    public String getArtist() { return artist; }
    public String getMbid() { return mbid; }
    public String getUrl() { return url; }
    public int getRank() { return rank; }
    public String getTs() { return ts; }
    public String getSs() { return ss; }
    public List<Tag> getTags() { return tags; }
}
