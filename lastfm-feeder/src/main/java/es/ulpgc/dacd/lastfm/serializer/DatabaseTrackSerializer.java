package es.ulpgc.dacd.lastfm.serializer;

import es.ulpgc.dacd.lastfm.model.Track;

public class DatabaseTrackSerializer implements TrackSerializer {

    private final String dbPath;

    public DatabaseTrackSerializer(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void serialize(Track track) {
        // TODO: implementar en el Paso 4
    }
}
