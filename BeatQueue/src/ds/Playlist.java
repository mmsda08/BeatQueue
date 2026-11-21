package ds;

import java.util.ArrayList;
import java.util.List;
import model.Song;

/**
 * Playlist as a DOUBLY LINKED LIST with a "now playing" pointer.
 * Time: addLast O(1), insertAt O(n), deleteAt O(n), next O(1), previous O(1).
 */
public class Playlist {

    private static class Node {
        final Song song;
        Node prev, next;

        Node(Song song) {
            this.song = song;
        }
    }

    private Node head, tail, current;
    private int size;

    public void addLast(Song s) {
        insertAt(size, s);
    }

    /** Inserts so that the new song ends up at the given index (0 = first, size = last). */
    public void insertAt(int index, Song s) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("index " + index);
        Node n = new Node(s);
        if (size == 0) {
            head = tail = n;
        } else if (index == size) {
            n.prev = tail;
            tail.next = n;
            tail = n;
        } else if (index == 0) {
            n.next = head;
            head.prev = n;
            head = n;
        } else {
            Node at = nodeAt(index);
            n.prev = at.prev;
            n.next = at;
            at.prev.next = n;
            at.prev = n;
        }
        size++;
        if (current == null) current = n;
    }

    public Song deleteAt(int index) {
        Node n = nodeAt(index);
        if (n == current) current = (n.next != null) ? n.next : n.prev;
        if (n.prev != null) n.prev.next = n.next;
        else head = n.next;
        if (n.next != null) n.next.prev = n.prev;
        else tail = n.prev;
        size--;
        return n.song;
    }

    /** Moves "now playing" forward. Returns false if already at the end. */
    public boolean next() {
        if (current != null && current.next != null) {
            current = current.next;
            return true;
        }
        return false;
    }

    /** Moves "now playing" back. Returns false if already at the start. */
    public boolean previous() {
        if (current != null && current.prev != null) {
            current = current.prev;
            return true;
        }
        return false;
    }

    public Song getCurrent() {
        return current == null ? null : current.song;
    }

    public int getCurrentIndex() {
        int i = 0;
        for (Node n = head; n != null; n = n.next, i++) {
            if (n == current) return i;
        }
        return -1;
    }

    public void setCurrentIndex(int index) {
        current = nodeAt(index);
    }

    /** Replaces the whole order (used by shuffle and by undo of shuffle). Keeps the same song playing. */
    public void setAll(List<Song> songs) {
        Song playing = getCurrent();
        head = tail = current = null;
        size = 0;
        for (Song s : songs) addLast(s);
        current = head;
        for (Node n = head; n != null; n = n.next) {
            if (n.song == playing) {
                current = n;
                break;
            }
        }
    }

    public List<Song> toList() {
        List<Song> out = new ArrayList<>();
        for (Node n = head; n != null; n = n.next) out.add(n.song);
        return out;
    }

    public int size() {
        return size;
    }

    private Node nodeAt(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("index " + index);
        Node n;
        if (index < size / 2) {
            n = head;
            for (int i = 0; i < index; i++) n = n.next;
        } else {
            n = tail;
            for (int i = size - 1; i > index; i--) n = n.prev;
        }
        return n;
    }
}
