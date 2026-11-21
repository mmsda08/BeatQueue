package model;

import java.util.List;

/** One undoable action. Holds just enough data to reverse itself. */
public class Action {
    public enum Type { REQUEST, VOTE, PLAY_NEXT, PLAYLIST_INSERT, PLAYLIST_DELETE, SHUFFLE, ADD_SONG }

    private final Type type;
    private final String description;
    private final Request request;
    private final Song song;
    private final int index;
    private final List<Song> snapshot;

    public Action(Type type, String description, Request request, Song song, int index, List<Song> snapshot) {
        this.type = type;
        this.description = description;
        this.request = request;
        this.song = song;
        this.index = index;
        this.snapshot = snapshot;
    }

    public Type getType() { return type; }
    public String getDescription() { return description; }
    public Request getRequest() { return request; }
    public Song getSong() { return song; }
    public int getIndex() { return index; }
    public List<Song> getSnapshot() { return snapshot; }
}
