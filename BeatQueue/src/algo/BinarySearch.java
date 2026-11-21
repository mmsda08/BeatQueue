package algo;

import java.util.ArrayList;
import java.util.List;
import model.Song;

/**
 * Binary search over a song list that is sorted by title (case-insensitive).
 * Time: O(log n) to find the first match.
 */
public class BinarySearch {

    /** Index of the first song whose title is >= the given title (the "lower bound"). */
    public static int lowerBound(List<Song> sortedByTitle, String title) {
        int lo = 0, hi = sortedByTitle.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (sortedByTitle.get(mid).getTitle().compareToIgnoreCase(title) < 0) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    /** All songs whose title equals the given title exactly (ignoring case). */
    public static List<Song> findAllByTitle(List<Song> sortedByTitle, String title) {
        List<Song> out = new ArrayList<>();
        String t = title.trim();
        int i = lowerBound(sortedByTitle, t);
        while (i < sortedByTitle.size() && sortedByTitle.get(i).getTitle().compareToIgnoreCase(t) == 0) {
            out.add(sortedByTitle.get(i));
            i++;
        }
        return out;
    }

    /** Index of a song with the same title AND artist, or -1. Used to catch duplicates. */
    public static int findDuplicate(List<Song> sortedByTitle, String title, String artist) {
        String t = title.trim();
        String a = artist.trim();
        int i = lowerBound(sortedByTitle, t);
        while (i < sortedByTitle.size() && sortedByTitle.get(i).getTitle().compareToIgnoreCase(t) == 0) {
            if (sortedByTitle.get(i).getArtist().equalsIgnoreCase(a)) return i;
            i++;
        }
        return -1;
    }
}
