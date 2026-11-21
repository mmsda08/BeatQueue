package algo;

import java.util.List;
import java.util.Random;

/**
 * Fisher-Yates shuffle: every ordering is equally likely.
 * Time: O(n). Extra space: O(1).
 */
public class FisherYates {

    /** Shuffles the given list in place. */
    public static <T> void shuffle(List<T> list, Random rnd) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1); // 0 <= j <= i
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }
}
