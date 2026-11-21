package ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Song;

/**
 * Top-K using a MIN-heap of size K: the weakest of the current top K sits on top.
 * A new song only replaces it if it is better. No full sort of the library is needed.
 * Time: O(n log K) for n songs. Extra space: O(K).
 * Ties: more votes wins; if votes are equal, the smaller song ID wins.
 */
public class TopKHeap {

    private final Song[] heap;
    private int size;

    public TopKHeap(int k) {
        heap = new Song[Math.max(1, k)];
    }

    /** true when a is a weaker candidate than b. */
    private boolean worse(Song a, Song b) {
        if (a.getVotes() != b.getVotes()) return a.getVotes() < b.getVotes();
        return a.getId() > b.getId();
    }

    public void offer(Song s) {
        if (s.getVotes() <= 0) return; // never voted -> not on the chart
        if (size < heap.length) {
            heap[size] = s;
            siftUp(size);
            size++;
        } else if (worse(heap[0], s)) {
            heap[0] = s;
            siftDown(0);
        }
    }

    /** Empties the heap and returns its songs, best first. */
    public List<Song> drainBestFirst() {
        List<Song> out = new ArrayList<>();
        while (size > 0) {
            out.add(heap[0]);
            heap[0] = heap[--size];
            heap[size] = null;
            if (size > 0) siftDown(0);
        }
        Collections.reverse(out); // weakest came out first
        return out;
    }

    public static List<Song> topK(List<Song> songs, int k) {
        TopKHeap h = new TopKHeap(k);
        for (Song s : songs) h.offer(s);
        return h.drainBestFirst();
    }

    private void siftUp(int i) {
        while (i > 0) {
            int p = (i - 1) / 2;
            if (worse(heap[i], heap[p])) {
                swap(i, p);
                i = p;
            } else break;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int l = 2 * i + 1, r = l + 1, w = i;
            if (l < size && worse(heap[l], heap[w])) w = l;
            if (r < size && worse(heap[r], heap[w])) w = r;
            if (w == i) break;
            swap(i, w);
            i = w;
        }
    }

    private void swap(int a, int b) {
        Song t = heap[a];
        heap[a] = heap[b];
        heap[b] = t;
    }
}
