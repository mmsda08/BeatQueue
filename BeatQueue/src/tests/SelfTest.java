package tests;

import algo.BinarySearch;
import algo.FisherYates;
import algo.MergeSort;
import controller.BeatQueueController;
import ds.HashTable;
import ds.Playlist;
import ds.RequestQueue;
import ds.TopKHeap;
import ds.Trie;
import ds.VotePriorityQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.Request;
import model.Song;

/** Console checks for every data structure. Run this file: it prints PASS or FAIL for each check. */
public class SelfTest {
    private static int failed = 0;

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failed++;
    }

    public static void main(String[] args) {
        // Queue: first in, first out
        RequestQueue q = new RequestQueue();
        Song a = new Song(1, "A", "x"), b = new Song(2, "B", "y"), c = new Song(3, "C", "z");
        Request r1 = new Request(1, a, 1), r2 = new Request(2, b, 2), r3 = new Request(3, c, 3);
        q.enqueue(r1); q.enqueue(r2); q.enqueue(r3);
        check("Queue dequeues in arrival order", q.dequeue() == r1 && q.peek() == r2);
        q.remove(r3);
        q.insertBySeq(r1);
        check("Queue insertBySeq restores arrival order", q.toList().get(0) == r1 && q.size() == 2);

        // Max-heap: most votes first, earlier request wins ties
        VotePriorityQueue h = new VotePriorityQueue();
        Request h1 = new Request(1, a, 1), h2 = new Request(2, b, 2), h3 = new Request(3, c, 3);
        h.insert(h1); h.insert(h2); h.insert(h3);
        check("Heap tie goes to the earlier request", h.peek() == h1);
        h3.addVote(1); h3.addVote(1); h.update(h3);
        check("Heap puts the most voted request on top", h.peek() == h3);
        h3.addVote(-2); h.update(h3);
        check("Heap returns to earliest after votes drop", h.extractMax() == h1 && h.extractMax() == h2);

        // Doubly linked list
        Playlist p = new Playlist();
        p.addLast(a); p.addLast(c); p.insertAt(1, b);
        check("Playlist insert in the middle", p.toList().get(1) == b && p.size() == 3);
        p.next(); p.next();
        check("Playlist next moves forward", p.getCurrent() == c && !p.next());
        p.previous();
        check("Playlist previous moves back", p.getCurrent() == b);
        p.deleteAt(1);
        check("Playlist delete fixes the pointer", p.size() == 2 && p.getCurrent() == c);

        // Hash table (with resizing)
        HashTable<Integer, String> ht = new HashTable<>();
        for (int i = 0; i < 1000; i++) ht.put(i, "v" + i);
        ht.remove(500);
        check("Hash table get / remove / resize", "v999".equals(ht.get(999)) && ht.get(500) == null && ht.size() == 999);

        // Trie
        Trie t = new Trie();
        Song s1 = new Song(1, "Dancing Queen", "ABBA"), s2 = new Song(2, "Levitating", "Dua Lipa"),
                s3 = new Song(3, "Don't Start Now", "Dua Lipa");
        t.insert(s1); t.insert(s2); t.insert(s3);
        check("Trie matches start of a title word", t.search("que").size() == 1);
        check("Trie matches start of an artist word", t.search("lip").size() == 2);
        check("Trie matches several words together", t.search("dua lev").size() == 1);
        check("Trie returns nothing for no match", t.search("zzz").isEmpty());

        // Merge sort (stable)
        List<Integer> nums = new ArrayList<>();
        Random rnd = new Random(7);
        for (int i = 0; i < 500; i++) nums.add(rnd.nextInt(100));
        List<Integer> sorted = MergeSort.sort(nums, Integer::compare);
        boolean ok = true;
        for (int i = 1; i < sorted.size(); i++) if (sorted.get(i - 1) > sorted.get(i)) ok = false;
        check("Merge sort sorts 500 numbers", ok && sorted.size() == 500);

        // Fisher-Yates
        List<Integer> deck = new ArrayList<>();
        for (int i = 0; i < 20; i++) deck.add(i);
        FisherYates.shuffle(deck, new Random(1));
        int sum = 0;
        for (int x : deck) sum += x;
        check("Shuffle keeps all items", sum == 190 && deck.size() == 20 && !deck.get(0).equals(0));

        // Top-K heap
        List<Song> many = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            Song s = new Song(i, "S" + i, "x");
            s.addVotes(i % 12);
            many.add(s);
        }
        List<Song> top = TopKHeap.topK(many, 10);
        boolean desc = true;
        for (int i = 1; i < top.size(); i++) if (top.get(i - 1).getVotes() < top.get(i).getVotes()) desc = false;
        check("Top-K returns 10 songs, best first", top.size() == 10 && desc && top.get(0).getVotes() == 11);

        // Binary search
        List<Song> lib = new ArrayList<>();
        lib.add(new Song(1, "Alpha", "A")); lib.add(new Song(2, "Beta", "B")); lib.add(new Song(3, "Beta", "C"));
        lib.add(new Song(4, "Gamma", "D"));
        check("Binary search finds all exact titles", BinarySearch.findAllByTitle(lib, "beta").size() == 2);
        check("Binary search detects a duplicate", BinarySearch.findDuplicate(lib, "Beta", "c") == 2
                && BinarySearch.findDuplicate(lib, "Beta", "Z") == -1);

        // Whole system through the controller
        BeatQueueController app = new BeatQueueController();
        Song song1 = app.findExactTitle("Levitating").get(0);
        Song song2 = app.findExactTitle("Perfect").get(0);
        check("Request works", app.requestSong(song1.getId()) == null && app.requestSong(song2.getId()) == null);
        check("Duplicate request is rejected", app.requestSong(song1.getId()) != null);
        app.vote(app.getPendingRequests().get(1).getRequestId());
        check("Vote moves a song to the top of the play order", app.getNextRequest().getSong() == song2);
        app.playNext();
        check("Play next fills the playlist", app.getPlaylist().size() == 1 && app.getPendingRequests().size() == 1);
        app.undo();
        check("Undo of play next restores the request", app.getPlaylist().isEmpty() && app.getPendingRequests().size() == 2
                && app.getNextRequest().getSong() == song2);
        app.undo(); app.undo(); app.undo(); // vote, request 2, request 1
        check("Undo of votes and requests clears everything", app.getPendingRequests().isEmpty()
                && song1.getVotes() == 0 && song2.getVotes() == 0);
        check("Duplicate song is rejected", app.addSong("levitating", "dua lipa") != null);
        check("New song is accepted and searchable", app.addSong("Hello", "Adele") == null
                && app.search("hel", "Title").size() == 1);
        app.undo();
        check("Undo of add song removes it", app.search("hel", "Title").isEmpty());

        System.out.println(failed == 0 ? "\nAll checks passed." : "\n" + failed + " check(s) FAILED.");
    }
}
