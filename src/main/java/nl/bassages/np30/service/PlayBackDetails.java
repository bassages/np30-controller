package nl.bassages.np30.service;

public class PlayBackDetails {
    private final String artist;
    private final String title;
    private final String album;

    public PlayBackDetails(final String artist, final String title, final String album) {
        this.artist = artist;
        this.title = title;
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }
}
