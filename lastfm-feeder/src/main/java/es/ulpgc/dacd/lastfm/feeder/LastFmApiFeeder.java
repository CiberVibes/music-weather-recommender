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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            Map<String, Track> merged = new LinkedHashMap<>();
            for (Track t : getCountryTopTracks(country)) merged.put(key(t), t);
            for (Track t : getGlobalTopTracks()) merged.putIfAbsent(key(t), t);
            return new ArrayList<>(merged.values()).stream()
                    .map(this::enrichWithTags)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String key(Track t) {
        return t.getName().toLowerCase() + "|" + t.getArtist().toLowerCase();
    }

    private Track enrichWithTags(Track track) {
        List<Tag> tags = getTopTagsForTrack(track.getArtist(), track.getName());
        return new Track(track.getName(), track.getArtist(), track.getMbid(), track.getUrl(), track.getRank(), track.getCapturedAt(), tags);
    }

    private List<Track> getCountryTopTracks(String country) throws IOException {
        String url = BASE_URL + "?method=geo.gettoptracks"
                + "&country=" + encode(country)
                + "&limit=300"
                + commonParams();
        return parseTopTracks(get(url));
    }

    private List<Track> getGlobalTopTracks() throws IOException {
        String url = BASE_URL + "?method=chart.gettoptracks"
                + "&limit=300"
                + commonParams();
        return parseTopTracks(get(url));
    }

    private List<Tag> getTopTagsForTrack(String artist, String trackName) {
        try {
            String url = BASE_URL + "?method=track.gettoptags"
                    + "&artist=" + encode(artist)
                    + "&track=" + encode(trackName)
                    + commonParams();
            return parseTopTags(get(url));
        } catch (IOException e) {
            return List.of();
        }
    }

    private String commonParams() {
        return "&api_key=" + apiKey + "&format=json";
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
        var items = response.tracks.track;
        List<Track> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            var t = items.get(i);
            int rank = (t.attr != null && t.attr.rank != null) ? Integer.parseInt(t.attr.rank) : i + 1;
            result.add(new Track(t.name, t.artist.name, t.mbid, t.url, rank, capturedAt, List.of()));
        }
        return result;
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
