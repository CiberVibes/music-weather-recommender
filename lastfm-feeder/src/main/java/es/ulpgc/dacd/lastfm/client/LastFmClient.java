package es.ulpgc.dacd.lastfm.client;

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

public class LastFmClient {

    private final String apiKey;

    public LastFmClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<Track> getTopTracks(String country) throws IOException {
        OkHttpClient httpClient = new OkHttpClient();
        Gson gson = new Gson();
        String url = "http://ws.audioscrobbler.com/2.0/?method=geo.gettoptracks&country=" + URLEncoder.encode(country, StandardCharsets.UTF_8) + "&api_key=" + apiKey + "&format=json";
        Request request = new Request.Builder().url(url).build();
        Response response = httpClient.newCall(request).execute();
        String json = response.body().string();
        TopTracksResponse r = gson.fromJson(json, TopTracksResponse.class);
        Instant capturedAt = Instant.now();
        return r.tracks.track.stream()
                .map(t -> new Track(t.name, t.artist.name, t.mbid, t.url, Integer.parseInt(t.attr.rank), capturedAt))
                .toList();
    }

    public List<Tag> getTopTagsForTrack(String artist, String trackName) throws IOException {
        OkHttpClient httpClient = new OkHttpClient();
        Gson gson = new Gson();
        String url = "http://ws.audioscrobbler.com/2.0/?method=track.gettoptags&artist=" + URLEncoder.encode(artist, StandardCharsets.UTF_8) + "&track=" + URLEncoder.encode(trackName, StandardCharsets.UTF_8) + "&api_key=" + apiKey + "&format=json";
        Request request = new Request.Builder().url(url).build();
        Response response = httpClient.newCall(request).execute();
        String json = response.body().string();
        TopTagsResponse r = gson.fromJson(json, TopTagsResponse.class);
        return r.toptags.tag.stream()
                .map(t -> new Tag(t.name, t.count))
                .toList();
    }

    public List<Track> getTopTracksByTag(String tag) throws IOException {
        OkHttpClient httpClient = new OkHttpClient();
        Gson gson = new Gson();
        String url = "http://ws.audioscrobbler.com/2.0/?method=tag.gettoptracks&tag=" + URLEncoder.encode(tag, StandardCharsets.UTF_8) + "&api_key=" + apiKey + "&format=json";
        Request request = new Request.Builder().url(url).build();
        Response response = httpClient.newCall(request).execute();
        String json = response.body().string();
        TopTracksResponse r = gson.fromJson(json, TopTracksResponse.class);
        Instant capturedAt = Instant.now();
        return r.tracks.track.stream()
                .map(t -> new Track(t.name, t.artist.name, t.mbid, t.url, Integer.parseInt(t.attr.rank), capturedAt))
                .toList();
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
