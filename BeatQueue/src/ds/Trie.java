package ds;

import java.util.ArrayList;
import java.util.List;
import model.Song;

/**
 * Trie (prefix tree) for autocomplete.
 * Every word of the title AND the artist is inserted, so typing the start of ANY word finds the song.
 * If the query has several words ("dua li"), a song must match all of them.
 * Characters: a-z and 0-9 (case-insensitive). Everything else separates words.
 * Time: insert O(L) per word, search O(L + size of the matching subtree).
 */
public class Trie {

    private static final int R = 36;

    private static class Node {
        final Node[] kids = new Node[R];
        final List<Song> songs = new ArrayList<>(2); // songs that have a word ending here
    }

    private Node root = new Node();

    private static int code(char c) {
        c = Character.toLowerCase(c);
        if (c >= 'a' && c <= 'z') return c - 'a';
        if (c >= '0' && c <= '9') return 26 + (c - '0');
        return -1;
    }

    private static List<String> words(String text) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (code(c) >= 0) {
                sb.append(Character.toLowerCase(c));
            } else if (sb.length() > 0) {
                out.add(sb.toString());
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) out.add(sb.toString());
        return out;
    }

    public void clear() {
        root = new Node();
    }

    public void insert(Song song) {
        for (String w : words(song.getTitle() + " " + song.getArtist())) {
            Node n = root;
            for (int i = 0; i < w.length(); i++) {
                int c = code(w.charAt(i));
                if (n.kids[c] == null) n.kids[c] = new Node();
                n = n.kids[c];
            }
            if (!n.songs.contains(song)) n.songs.add(song);
        }
    }

    /** Songs where every word of the query is the start of some word in the title or artist. */
    public List<Song> search(String query) {
        List<String> tokens = words(query);
        List<Song> result = new ArrayList<>();
        if (tokens.isEmpty()) return result;

        for (int t = 0; t < tokens.size(); t++) {
            List<Song> found = new ArrayList<>();
            HashTable<Integer, Boolean> ids = new HashTable<>();
            Node n = find(tokens.get(t));
            if (n != null) collect(n, found, ids);

            if (t == 0) {
                result = found;
            } else {
                List<Song> keep = new ArrayList<>();
                for (Song s : result) {
                    if (ids.containsKey(s.getId())) keep.add(s);
                }
                result = keep;
            }
            if (result.isEmpty()) break;
        }
        return result;
    }

    private Node find(String prefix) {
        Node n = root;
        for (int i = 0; i < prefix.length(); i++) {
            n = n.kids[code(prefix.charAt(i))];
            if (n == null) return null;
        }
        return n;
    }

    private void collect(Node n, List<Song> out, HashTable<Integer, Boolean> seen) {
        for (Song s : n.songs) {
            if (!seen.containsKey(s.getId())) {
                seen.put(s.getId(), true);
                out.add(s);
            }
        }
        for (Node k : n.kids) {
            if (k != null) collect(k, out, seen);
        }
    }
}
