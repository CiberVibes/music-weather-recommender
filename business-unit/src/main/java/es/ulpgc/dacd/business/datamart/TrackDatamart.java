package es.ulpgc.dacd.business.datamart;

import es.ulpgc.dacd.business.model.Tag;
import es.ulpgc.dacd.business.model.Track;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TrackDatamart {

    private static final Logger logger = Logger.getLogger(TrackDatamart.class.getName());

    private final String dbPath;

    public TrackDatamart(String dbPath) {
        this.dbPath = dbPath;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection connection = connect()) {
            createTracksTable(connection);
            createTrackTagsTable(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTracksTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS tracks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    artist TEXT NOT NULL,
                    mbid TEXT,
                    url TEXT,
                    rank INTEGER,
                    ts TEXT NOT NULL,
                    ss TEXT NOT NULL,
                    UNIQUE(name, artist)
                )""";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void createTrackTagsTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS track_tags (
                    track_id INTEGER NOT NULL,
                    tag_name TEXT NOT NULL,
                    tag_count INTEGER,
                    FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE
                )""";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void save(Track track) {
        try (Connection connection = connect()) {
            long trackId = upsertTrack(connection, track);
            replaceTrackTags(connection, trackId, track.getTags());
        } catch (SQLException e) {
            logger.severe("Failed to save track '" + track.getName() + "': " + e.getMessage());
        }
    }

    private long upsertTrack(Connection connection, Track track) throws SQLException {
        String sql = """
                INSERT INTO tracks (name, artist, mbid, url, rank, ts, ss)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(name, artist) DO UPDATE SET
                    rank = excluded.rank,
                    ts = excluded.ts
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, track.getName());
            stmt.setString(2, track.getArtist());
            stmt.setString(3, track.getMbid());
            stmt.setString(4, track.getUrl());
            stmt.setInt(5, track.getRank());
            stmt.setString(6, track.getTs());
            stmt.setString(7, track.getSs());
            stmt.executeUpdate();
        }
        return getTrackId(connection, track.getName(), track.getArtist());
    }

    private long getTrackId(Connection connection, String name, String artist) throws SQLException {
        String sql = "SELECT id FROM tracks WHERE name = ? AND artist = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, artist);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
        }
        throw new SQLException("Track not found after upsert: " + name);
    }

    private void replaceTrackTags(Connection connection, long trackId, List<Tag> tags) throws SQLException {
        try (PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM track_tags WHERE track_id = ?")) {
            deleteStmt.setLong(1, trackId);
            deleteStmt.executeUpdate();
        }
        String sql = "INSERT INTO track_tags (track_id, tag_name, tag_count) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Tag tag : tags) {
                stmt.setLong(1, trackId);
                stmt.setString(2, tag.getName());
                stmt.setInt(3, tag.getCount());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<Track> findByTag(String tagName) {
        String sql = """
                SELECT t.name, t.artist, t.mbid, t.url, t.rank, t.ts, t.ss
                FROM tracks t
                JOIN track_tags tt ON t.id = tt.track_id
                WHERE LOWER(tt.tag_name) = LOWER(?)
                ORDER BY t.rank
                """;
        List<Track> result = new ArrayList<>();
        try (Connection connection = connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tagName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(new Track(
                        rs.getString("name"), rs.getString("artist"),
                        rs.getString("mbid"), rs.getString("url"),
                        rs.getInt("rank"), rs.getString("ts"),
                        rs.getString("ss"), List.of()
                ));
            }
        } catch (SQLException e) {
            logger.severe("Failed to query tracks by tag: " + e.getMessage());
        }
        return result;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
}
