package algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Stable merge sort written by hand.
 * Time: O(n log n) in every case. Extra space: O(n).
 */
public class MergeSort {

    /** Returns a NEW sorted list; the input list is not changed. */
    @SuppressWarnings("unchecked")
    public static <T> List<T> sort(List<T> input, Comparator<? super T> cmp) {
        T[] a = (T[]) input.toArray();
        T[] tmp = (T[]) new Object[a.length];
        mergeSort(a, tmp, 0, a.length - 1, cmp);
        return new ArrayList<>(Arrays.asList(a));
    }

    private static <T> void mergeSort(T[] a, T[] tmp, int lo, int hi, Comparator<? super T> cmp) {
        if (lo >= hi) return;
        int mid = (lo + hi) >>> 1;
        mergeSort(a, tmp, lo, mid, cmp);
        mergeSort(a, tmp, mid + 1, hi, cmp);
        merge(a, tmp, lo, mid, hi, cmp);
    }

    private static <T> void merge(T[] a, T[] tmp, int lo, int mid, int hi, Comparator<? super T> cmp) {
        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi) {
            // take from the right only when it is strictly smaller -> keeps the sort stable
            if (cmp.compare(a[j], a[i]) < 0) tmp[k++] = a[j++];
            else tmp[k++] = a[i++];
        }
        while (i <= mid) tmp[k++] = a[i++];
        while (j <= hi) tmp[k++] = a[j++];
        for (k = lo; k <= hi; k++) a[k] = tmp[k];
    }
}
