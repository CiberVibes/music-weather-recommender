package es.ulpgc.dacd.lastfm.model;

public class TrackTag {
    private final String trackName;
    private final String artist;
    private final String tagName;
    private final int tagCount;

    public TrackTag(String trackName, String artist, String tagName, int tagCount) {
        this.trackName = trackName;
        this.artist = artist;
        this.tagName = tagName;
        this.tagCount = tagCount;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getArtist() {
        return artist;
    }

    public String getTagName() {
        return tagName;
    }

    public int getTagCount() {
        return tagCount;
    }
}
