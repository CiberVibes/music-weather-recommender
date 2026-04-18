package es.ulpgc.dacd.lastfm.feeder;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import es.ulpgc.dacd.lastfm.model.Tag;
import es.ulpgc.dacd.lastfm.model.Track;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

public class LastFmApiFeeder implements LastFmFeeder {

    private static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/";

    private final String apiKey;
    private final String country;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public LastFmApiFeeder(String apiKey, String country) {
        this.apiKey = apiKey;
        this.country = country;
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    @Override
    public List<Track> feed() {
        try {
            List<Track> tracks = getTopTracks(country);
            return tracks.stream()
                    .map(t -> new Track(t.getName(), t.getArtist(), t.getMbid(), t.getUrl(), t.getRank(), t.getCapturedAt(), getTopTagsForTrack(t.getArtist(), t.getName())))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Track> getTopTracks(String country) throws IOException {
        String url = BASE_URL + "?method=geo.gettoptracks"
                + "&country=" + encode(country)
                + "&api_key=" + apiKey
                + "&format=json";
        String json = get(url);
        return parseTopTracks(json);
    }

    private List<Tag> getTopTagsForTrack(String artist, String trackName) {
        try {
            String url = BASE_URL + "?method=track.gettoptags"
                    + "&artist=" + encode(artist)
                    + "&track=" + encode(trackName)
                    + "&api_key=" + apiKey
                    + "&format=json";
            String json = get(url);
            return parseTopTags(json);
        } catch (IOException e) {
            return List.of();
        }
    }

    private String get(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response: " + response);
            return response.body().string();
        }
    }

    private List<Track> parseTopTracks(String json) {
        TopTracksResponse response = gson.fromJson(json, TopTracksResponse.class);
        Instant capturedAt = Instant.now();
        return response.tracks.track.stream()
                .map(t -> new Track(t.name, t.artist.name, t.mbid, t.url, Integer.parseInt(t.attr.rank), capturedAt, List.of()))
                .toList();
    }

    private List<Tag> parseTopTags(String json) {
        TopTagsResponse response = gson.fromJson(json, TopTagsResponse.class);
        return response.toptags.tag.stream()
                .map(t -> new Tag(t.name, t.count))
                .toList();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static class TopTracksResponse {
        Tracks tracks;

        private static class Tracks {
            List<TrackItem> track;
        }

        private static class TrackItem {
            String name;
            String mbid;
            String url;
            ArtistItem artist;
            @SerializedName("@attr")
            AttrItem attr;
        }

        private static class ArtistItem {
            String name;
        }

        private static class AttrItem {
            String rank;
        }
    }

    private static class TopTagsResponse {
        TopTags toptags;

        private static class TopTags {
            List<TagItem> tag;
        }

        private static class TagItem {
            String name;
            int count;
        }
    }
}
