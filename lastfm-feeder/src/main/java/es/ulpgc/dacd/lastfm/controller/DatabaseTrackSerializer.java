package es.ulpgc.dacd.lastfm.controller;

import es.ulpgc.dacd.lastfm.model.Tag;
import es.ulpgc.dacd.lastfm.model.Track;

import java.sql.*;

public class DatabaseTrackSerializer implements TrackSerializer {

    private final String dbPath;

    public DatabaseTrackSerializer(String dbPath) {
        this.dbPath = dbPath;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection connection = connect()) {
            createTablesIfNotExist(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void serialize(Track track) {
        try (Connection connection = connect()) {
            insertTrack(connection, track);
            insertTags(connection, track);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTablesIfNotExist(Connection connection) throws SQLException {
        createTracksTable(connection);
        createTagsTable(connection);
    }

    private void createTracksTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS tracks (
                    name TEXT,
                    artist TEXT,
                    mbid TEXT,
                    url TEXT,
                    rank INTEGER,
                    captured_at TEXT,
                    PRIMARY KEY (name, artist)
                )""";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void createTagsTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS tags (
                    track_name TEXT,
                    track_artist TEXT,
                    tag_name TEXT,
                    tag_count INTEGER,
                    FOREIGN KEY (track_name, track_artist) REFERENCES tracks(name, artist)
                )""";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void insertTrack(Connection connection, Track track) throws SQLException {
        String sql = """
                INSERT OR REPLACE INTO tracks (name, artist, mbid, url, rank, captured_at)
                VALUES (?, ?, ?, ?, ?, ?)""";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, track.getName());
            stmt.setString(2, track.getArtist());
            stmt.setString(3, track.getMbid());
            stmt.setString(4, track.getUrl());
            stmt.setInt(5, track.getRank());
            stmt.setString(6, track.getCapturedAt().toString());
            stmt.executeUpdate();
        }
    }

    private void insertTags(Connection connection, Track track) throws SQLException {
        String deleteSql = "DELETE FROM tags WHERE track_name = ? AND track_artist = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setString(1, track.getName());
            stmt.setString(2, track.getArtist());
            stmt.executeUpdate();
        }
        String insertSql = "INSERT INTO tags (track_name, track_artist, tag_name, tag_count) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            for (Tag tag : track.getTags()) {
                stmt.setString(1, track.getName());
                stmt.setString(2, track.getArtist());
                stmt.setString(3, tag.getName());
                stmt.setInt(4, tag.getCount());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
}
