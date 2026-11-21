package ds;

import java.util.ArrayList;
import java.util.List;
import model.Action;

/**
 * Stack (LIFO) of actions, array based.
 * Used for Undo (pop) and for the "recent actions" list (newest first).
 * Time: push O(1) amortized, pop O(1), peek O(1).
 */
public class ActionStack {

    private Action[] items = new Action[32];
    private int size;

    public void push(Action a) {
        if (size == items.length) {
            Action[] bigger = new Action[items.length * 2];
            System.arraycopy(items, 0, bigger, 0, size);
            items = bigger;
        }
        items[size++] = a;
    }

    public Action pop() {
        if (size == 0) return null;
        Action a = items[--size];
        items[size] = null;
        return a;
    }

    public Action peek() {
        return size == 0 ? null : items[size - 1];
    }

    public int size() {
        return size;
    }

    /** Newest first. */
    public List<Action> newestFirst(int limit) {
        List<Action> out = new ArrayList<>();
        for (int i = size - 1; i >= 0 && out.size() < limit; i--) out.add(items[i]);
        return out;
    }
}
