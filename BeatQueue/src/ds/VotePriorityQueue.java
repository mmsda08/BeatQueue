package ds;

import model.Request;

/**
 * Priority queue implemented as an array-based MAX-HEAP.
 * The request with the most votes is on top. If votes are equal, the EARLIER request wins.
 * Each request remembers its heap index, so changing its votes costs only O(log n).
 * Time: insert O(log n), extractMax O(log n), peek O(1), update O(log n), remove O(log n).
 */
public class VotePriorityQueue {

    private Request[] heap = new Request[16];
    private int size;

    /** true when a should be closer to the top than b. */
    private boolean higher(Request a, Request b) {
        if (a.getVotes() != b.getVotes()) return a.getVotes() > b.getVotes();
        return a.getSeq() < b.getSeq(); // tie: earlier request wins
    }

    public void insert(Request r) {
        if (size == heap.length) {
            Request[] bigger = new Request[heap.length * 2];
            System.arraycopy(heap, 0, bigger, 0, size);
            heap = bigger;
        }
        heap[size] = r;
        r.heapIndex = size;
        size++;
        siftUp(size - 1);
    }

    public Request peek() {
        return size == 0 ? null : heap[0];
    }

    public Request extractMax() {
        if (size == 0) return null;
        Request top = heap[0];
        removeAt(0);
        return top;
    }

    /** Call after a request's vote count changed. */
    public void update(Request r) {
        int i = r.heapIndex;
        if (i < 0 || i >= size || heap[i] != r) return;
        siftUp(i);
        siftDown(r.heapIndex);
    }

    public boolean remove(Request r) {
        int i = r.heapIndex;
        if (i < 0 || i >= size || heap[i] != r) return false;
        removeAt(i);
        return true;
    }

    public int size() {
        return size;
    }

    private void removeAt(int i) {
        Request removed = heap[i];
        size--;
        Request last = heap[size];
        heap[size] = null;
        removed.heapIndex = -1;
        if (i < size) {
            heap[i] = last;
            last.heapIndex = i;
            siftUp(i);
            siftDown(last.heapIndex);
        }
    }

    private void siftUp(int i) {
        while (i > 0) {
            int p = (i - 1) / 2;
            if (higher(heap[i], heap[p])) {
                swap(i, p);
                i = p;
            } else break;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int l = 2 * i + 1, r = l + 1, best = i;
            if (l < size && higher(heap[l], heap[best])) best = l;
            if (r < size && higher(heap[r], heap[best])) best = r;
            if (best == i) break;
            swap(i, best);
            i = best;
        }
    }

    private void swap(int a, int b) {
        Request t = heap[a];
        heap[a] = heap[b];
        heap[b] = t;
        heap[a].heapIndex = a;
        heap[b].heapIndex = b;
    }
}
