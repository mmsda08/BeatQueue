package model;

/** A song in the library. "votes" is the total number of votes/requests it has received. */
public class Song {
    private final int id;
    private final String title;
    private final String artist;
    private int votes;

    public Song(int id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public int getVotes() { return votes; }
    public void addVotes(int delta) { votes += delta; }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}
