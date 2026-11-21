package model;

/** A pending song request. seq is the arrival order (smaller = earlier). */
public class Request {
    private final int requestId;
    private final Song song;
    private final long seq;
    private int votes;

    /** Position inside the max-heap. Kept up to date by VotePriorityQueue so updates are O(log n). */
    public int heapIndex = -1;

    public Request(int requestId, Song song, long seq) {
        this.requestId = requestId;
        this.song = song;
        this.seq = seq;
        this.votes = 1; // the person who requests the song gives the first vote
    }

    public int getRequestId() { return requestId; }
    public Song getSong() { return song; }
    public long getSeq() { return seq; }
    public int getVotes() { return votes; }
    public void addVote(int delta) { votes += delta; }
}
