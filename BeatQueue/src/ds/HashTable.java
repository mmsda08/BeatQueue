package ds;

/**
 * Hash table with separate chaining, written by hand. Resizes at load factor 0.75.
 * Time: get / put / remove / containsKey are O(1) on average, O(n) worst case.
 * Used for: lookup by song ID, lookup by request ID, and "is this song already requested?".
 */
public class HashTable<K, V> {

    private static class Entry<K, V> {
        final K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Entry<K, V>[] table;
    private int size;

    @SuppressWarnings("unchecked")
    public HashTable() {
        table = (Entry<K, V>[]) new Entry[16];
    }

    private static int indexFor(Object key, int length) {
        int h = key.hashCode();
        h ^= (h >>> 16);
        return (h & 0x7fffffff) % length;
    }

    public V get(K key) {
        for (Entry<K, V> e = table[indexFor(key, table.length)]; e != null; e = e.next) {
            if (e.key.equals(key)) return e.value;
        }
        return null;
    }

    public boolean containsKey(K key) {
        for (Entry<K, V> e = table[indexFor(key, table.length)]; e != null; e = e.next) {
            if (e.key.equals(key)) return true;
        }
        return false;
    }

    public void put(K key, V value) {
        int i = indexFor(key, table.length);
        for (Entry<K, V> e = table[i]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                e.value = value;
                return;
            }
        }
        table[i] = new Entry<>(key, value, table[i]);
        size++;
        if (size > table.length * 3 / 4) resize();
    }

    public V remove(K key) {
        int i = indexFor(key, table.length);
        Entry<K, V> prev = null;
        for (Entry<K, V> e = table[i]; e != null; prev = e, e = e.next) {
            if (e.key.equals(key)) {
                if (prev == null) table[i] = e.next;
                else prev.next = e.next;
                size--;
                return e.value;
            }
        }
        return null;
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] old = table;
        table = (Entry<K, V>[]) new Entry[old.length * 2];
        for (Entry<K, V> head : old) {
            for (Entry<K, V> e = head; e != null; ) {
                Entry<K, V> next = e.next;
                int i = indexFor(e.key, table.length);
                e.next = table[i];
                table[i] = e;
                e = next;
            }
        }
    }
}
