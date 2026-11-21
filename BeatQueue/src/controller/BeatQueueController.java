package controller;

import algo.BinarySearch;
import algo.FisherYates;
import algo.MergeSort;
import ds.ActionStack;
import ds.HashTable;
import ds.Playlist;
import ds.RequestQueue;
import ds.TopKHeap;
import ds.Trie;
import ds.VotePriorityQueue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import model.Action;
import model.Request;
import model.Song;

/**
 * Holds all data structures and the rules of BeatQueue.
 * The UI only talks to this class.
 * Methods that can fail return an error message (String), or null when everything went fine.
 */
public class BeatQueueController {

    public static final String SORT_TITLE = "Title";
    public static final String SORT_ARTIST = "Artist";
    public static final String SORT_VOTES = "Votes";

    // song library, always kept sorted by title so binary search works
    private final List<Song> library = new ArrayList<>();
    private final HashTable<Integer, Song> songsById = new HashTable<>();
    private final HashTable<Integer, Request> requestsById = new HashTable<>();
    private final HashTable<Integer, Request> pendingBySongId = new HashTable<>();

    private final RequestQueue queue = new RequestQueue();          // first-come, first-served
    private final VotePriorityQueue heap = new VotePriorityQueue(); // most votes first
    private final Playlist playlist = new Playlist();               // doubly linked list
    private final Trie trie = new Trie();                           // autocomplete
    private final ActionStack undoStack = new ActionStack();        // undo + recent actions

    private final List<Runnable> listeners = new ArrayList<>();
    private final Random random = new Random();

    private int nextSongId = 1;
    private int nextRequestId = 1;
    private long nextSeq = 1;

    public BeatQueueController() {
        String[][] seed = {
            {"Blinding Lights", "The Weeknd"}, {"Ere", "Juan Karlos"},
            {"Pare Ko", "Eraserheads"}, {"Ang Huling El Bimbo", "Eraserheads"},
            {"Tadhana", "Up Dharma Down"}, {"Mundo", "IV of Spades"},
            {"Kathang Isip", "Ben&Ben"}, {"Pagsamo", "Arthur Nery"},
            {"Tingin", "Cup of Joe"}, {"Levitating", "Dua Lipa"},
            {"Don't Start Now", "Dua Lipa"}, {"Shape of You", "Ed Sheeran"},
            {"Perfect", "Ed Sheeran"}, {"Someone Like You", "Adele"},
            {"Rolling in the Deep", "Adele"}, {"Bohemian Rhapsody", "Queen"},
            {"Dancing Queen", "ABBA"}, {"Uptown Funk", "Mark Ronson ft. Bruno Mars"},
            {"Locked Out of Heaven", "Bruno Mars"}, {"Believer", "Imagine Dragons"},
            {"Counting Stars", "OneRepublic"}, {"Viva La Vida", "Coldplay"},
            {"Yellow", "Coldplay"}, {"Sunflower", "Post Malone"},
            {"Bad Guy", "Billie Eilish"}, {"Stay", "The Kid LAROI & Justin Bieber"}
        };
        for (String[] s : seed) addSongInternal(s[0], s[1]);
    }

    // ------------------------------------------------------------ listeners

    public void addChangeListener(Runnable r) {
        listeners.add(r);
    }

    private void fire() {
        for (Runnable r : new ArrayList<>(listeners)) r.run();
    }

    // ------------------------------------------------------------ library / search

    private Song addSongInternal(String title, String artist) {
        Song s = new Song(nextSongId++, title.trim(), artist.trim());
        insertSorted(s);
        songsById.put(s.getId(), s);
        trie.insert(s);
        return s;
    }

    private void insertSorted(Song s) {
        library.add(BinarySearch.lowerBound(library, s.getTitle()), s);
    }

    private static Comparator<Song> comparatorFor(String sortKey) {
        if (SORT_ARTIST.equals(sortKey)) {
            return (a, b) -> {
                int c = a.getArtist().compareToIgnoreCase(b.getArtist());
                if (c == 0) c = a.getTitle().compareToIgnoreCase(b.getTitle());
                return c != 0 ? c : Integer.compare(a.getId(), b.getId());
            };
        }
        if (SORT_VOTES.equals(sortKey)) {
            return (a, b) -> {
                int c = Integer.compare(b.getVotes(), a.getVotes()); // most votes first
                if (c == 0) c = a.getTitle().compareToIgnoreCase(b.getTitle());
                return c != 0 ? c : Integer.compare(a.getId(), b.getId());
            };
        }
        return (a, b) -> {
            int c = a.getTitle().compareToIgnoreCase(b.getTitle());
            return c != 0 ? c : Integer.compare(a.getId(), b.getId());
        };
    }

    /** Whole library, sorted with merge sort by Title, Artist or Votes. */
    public List<Song> getLibrary(String sortKey) {
        return MergeSort.sort(library, comparatorFor(sortKey));
    }

    /** Trie autocomplete (start of any word in title or artist), merge-sorted. Empty query = whole library. */
    public List<Song> search(String query, String sortKey) {
        if (query == null || query.trim().isEmpty()) return getLibrary(sortKey);
        return MergeSort.sort(trie.search(query), comparatorFor(sortKey));
    }

    /** Exact title lookup in the sorted library with binary search. */
    public List<Song> findExactTitle(String title) {
        if (title == null || title.trim().isEmpty()) return new ArrayList<>();
        return BinarySearch.findAllByTitle(library, title);
    }

    public Song getSong(int id) {
        return songsById.get(id);
    }

    public String addSong(String title, String artist) {
        if (title == null || artist == null || title.trim().isEmpty() || artist.trim().isEmpty()) {
            return "Please type both a title and an artist.";
        }
        if (title.trim().length() > 60 || artist.trim().length() > 60) {
            return "Title and artist must be 60 characters or less.";
        }
        if (BinarySearch.findDuplicate(library, title, artist) >= 0) {
            return "Duplicate song: \"" + title.trim() + "\" by " + artist.trim() + " is already in the library.";
        }
        Song s = addSongInternal(title, artist);
        undoStack.push(new Action(Action.Type.ADD_SONG, "Added song \"" + s.getTitle() + "\" to the library",
                null, s, -1, null));
        fire();
        return null;
    }

    private void removeSongInternal(Song s) {
        library.remove(s);
        songsById.remove(s.getId());
        trie.clear();
        for (Song x : library) trie.insert(x);
    }

    // ------------------------------------------------------------ requests and votes

    public String requestSong(int songId) {
        Song song = songsById.get(songId);
        if (song == null) return "Song not found.";
        if (pendingBySongId.containsKey(songId)) {
            return "\"" + song.getTitle() + "\" is already requested. Vote for it in the Queue tab instead.";
        }
        Request r = new Request(nextRequestId++, song, nextSeq++);
        song.addVotes(1);
        queue.enqueue(r);
        heap.insert(r);
        requestsById.put(r.getRequestId(), r);
        pendingBySongId.put(songId, r);
        undoStack.push(new Action(Action.Type.REQUEST, "Requested \"" + song.getTitle() + "\"", r, song, -1, null));
        fire();
        return null;
    }

    public String vote(int requestId) {
        Request r = requestsById.get(requestId);
        if (r == null) return "That request is no longer in the queue.";
        r.addVote(1);
        r.getSong().addVotes(1);
        heap.update(r);
        undoStack.push(new Action(Action.Type.VOTE, "Voted for \"" + r.getSong().getTitle() + "\"", r,
                r.getSong(), -1, null));
        fire();
        return null;
    }

    public List<Request> getPendingRequests() {
        return queue.toList();
    }

    /** The request that will play next (most votes, earliest wins ties). */
    public Request getNextRequest() {
        return heap.peek();
    }

    public String playNext() {
        Request r = heap.extractMax();
        if (r == null) return "The request queue is empty.";
        queue.remove(r);
        requestsById.remove(r.getRequestId());
        pendingBySongId.remove(r.getSong().getId());
        playlist.addLast(r.getSong());
        undoStack.push(new Action(Action.Type.PLAY_NEXT, "Played \"" + r.getSong().getTitle() + "\" next", r,
                r.getSong(), playlist.size() - 1, null));
        fire();
        return null;
    }

    // ------------------------------------------------------------ playlist

    public List<Song> getPlaylist() {
        return playlist.toList();
    }

    public int getPlaylistCurrentIndex() {
        return playlist.getCurrentIndex();
    }

    public String playlistNext() {
        if (playlist.size() == 0) return "The playlist is empty.";
        if (!playlist.next()) return "This is the last song in the playlist.";
        fire();
        return null;
    }

    public String playlistPrevious() {
        if (playlist.size() == 0) return "The playlist is empty.";
        if (!playlist.previous()) return "This is the first song in the playlist.";
        fire();
        return null;
    }

    public String playlistPlayAt(int index) {
        if (index < 0 || index >= playlist.size()) return "Select a song in the playlist first.";
        playlist.setCurrentIndex(index);
        fire();
        return null;
    }

    /** position is 0-based; size() puts it at the end. */
    public String insertIntoPlaylist(int position, Song song) {
        if (song == null) return "Choose a song to insert.";
        int index = Math.max(0, Math.min(position, playlist.size()));
        playlist.insertAt(index, song);
        undoStack.push(new Action(Action.Type.PLAYLIST_INSERT,
                "Inserted \"" + song.getTitle() + "\" at position " + (index + 1), null, song, index, null));
        fire();
        return null;
    }

    public String deleteFromPlaylist(int index) {
        if (index < 0 || index >= playlist.size()) return "Select a song in the playlist first.";
        Song s = playlist.deleteAt(index);
        undoStack.push(new Action(Action.Type.PLAYLIST_DELETE, "Deleted \"" + s.getTitle() + "\" from the playlist",
                null, s, index, null));
        fire();
        return null;
    }

    public String shufflePlaylist() {
        if (playlist.size() < 2) return "Add at least 2 songs to shuffle.";
        List<Song> before = playlist.toList();
        List<Song> shuffled = new ArrayList<>(before);
        FisherYates.shuffle(shuffled, random);
        playlist.setAll(shuffled);
        undoStack.push(new Action(Action.Type.SHUFFLE, "Shuffled the playlist", null, null, -1, before));
        fire();
        return null;
    }

    // ------------------------------------------------------------ chart, undo, history

    /** Top 10 songs by votes, found with a size-10 min-heap (no full sort). */
    public List<Song> getTopSongs() {
        return TopKHeap.topK(library, 10);
    }

    public List<String> getRecentActions(int limit) {
        List<String> out = new ArrayList<>();
        for (Action a : undoStack.newestFirst(limit)) out.add(a.getDescription());
        return out;
    }

    /** Undoes the newest action. Returns a message for the status bar. */
    public String undo() {
        Action a = undoStack.pop();
        if (a == null) return "Nothing to undo.";
        switch (a.getType()) {
            case REQUEST: {
                Request r = a.getRequest();
                queue.remove(r);
                heap.remove(r);
                requestsById.remove(r.getRequestId());
                pendingBySongId.remove(r.getSong().getId());
                r.getSong().addVotes(-r.getVotes());
                break;
            }
            case VOTE: {
                Request r = a.getRequest();
                r.addVote(-1);
                r.getSong().addVotes(-1);
                heap.update(r);
                break;
            }
            case PLAY_NEXT: {
                Request r = a.getRequest();
                playlist.deleteAt(a.getIndex());
                heap.insert(r);
                queue.insertBySeq(r);
                requestsById.put(r.getRequestId(), r);
                pendingBySongId.put(r.getSong().getId(), r);
                break;
            }
            case PLAYLIST_INSERT:
                playlist.deleteAt(a.getIndex());
                break;
            case PLAYLIST_DELETE:
                playlist.insertAt(a.getIndex(), a.getSong());
                break;
            case SHUFFLE:
                playlist.setAll(a.getSnapshot());
                break;
            case ADD_SONG:
                removeSongInternal(a.getSong());
                break;
            default:
                break;
        }
        fire();
        return "Undid: " + a.getDescription();
    }
}
