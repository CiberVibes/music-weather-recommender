package es.ulpgc.dacd.lastfm.serializer;

import es.ulpgc.dacd.lastfm.model.Tag;
import es.ulpgc.dacd.lastfm.model.Track;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseTrackSerializer implements TrackSerializer {

    private final String dbPath;

    public DatabaseTrackSerializer(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void serialize(Track track) {
        try (Connection connection = connect()) {
            createTablesIfNotExist(connection);
            insertTrack(connection, track);
            insertTags(connection, track);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    private void createTablesIfNotExist(Connection connection) throws SQLException {
        createTracksTable(connection);
        createTagsTable(connection);
    }

    private void createTracksTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS tracks ("
                + "name TEXT NOT NULL,"
                + "artist TEXT NOT NULL,"
                + "mbid TEXT,"
                + "url TEXT,"
                + "rank INTEGER,"
                + "captured_at TEXT NOT NULL"
                + ")";
        connection.createStatement().execute(sql);
    }

    private void createTagsTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS track_tags ("
                + "track_name TEXT NOT NULL,"
                + "artist TEXT NOT NULL,"
                + "tag_name TEXT NOT NULL,"
                + "tag_count INTEGER"
                + ")";
        connection.createStatement().execute(sql);
    }

    private void insertTrack(Connection connection, Track track) throws SQLException {
        String sql = "INSERT INTO tracks (name, artist, mbid, url, rank, captured_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, track.getName());
            stmt.setString(2, track.getArtist());
            stmt.setString(3, track.getMbid());
            stmt.setString(4, track.getUrl());
            stmt.setInt(5, track.getRank());
            stmt.setString(6, track.getCapturedAt().toString());
            stmt.execute();
        }
    }

    private void insertTags(Connection connection, Track track) throws SQLException {
        for (Tag tag : track.getTags()) {
            insertTag(connection, track, tag);
        }
    }

    private void insertTag(Connection connection, Track track, Tag tag) throws SQLException {
        String sql = "INSERT INTO track_tags (track_name, artist, tag_name, tag_count) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, track.getName());
            stmt.setString(2, track.getArtist());
            stmt.setString(3, tag.getName());
            stmt.setInt(4, tag.getCount());
            stmt.execute();
        }
    }
}
