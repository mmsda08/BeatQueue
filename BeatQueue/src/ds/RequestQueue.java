package ds;

import java.util.ArrayList;
import java.util.List;
import model.Request;

/**
 * FIFO queue (first-come, first-served) built on a singly linked list.
 * Time: enqueue O(1), dequeue O(1), remove(request) O(n), insertBySeq O(n).
 */
public class RequestQueue {

    private static class Node {
        final Request request;
        Node next;

        Node(Request request) {
            this.request = request;
        }
    }

    private Node head, tail;
    private int size;

    public void enqueue(Request r) {
        Node n = new Node(r);
        if (tail == null) head = tail = n;
        else {
            tail.next = n;
            tail = n;
        }
        size++;
    }

    public Request dequeue() {
        if (head == null) return null;
        Request r = head.request;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return r;
    }

    public Request peek() {
        return head == null ? null : head.request;
    }

    /** Removes a specific request (used when the heap picks a request that is not at the front). */
    public boolean remove(Request r) {
        Node prev = null;
        for (Node cur = head; cur != null; prev = cur, cur = cur.next) {
            if (cur.request == r) {
                if (prev == null) head = cur.next;
                else prev.next = cur.next;
                if (cur == tail) tail = prev;
                size--;
                return true;
            }
        }
        return false;
    }

    /** Puts a request back at the place where it originally arrived (used by Undo). */
    public void insertBySeq(Request r) {
        Node n = new Node(r);
        if (head == null || r.getSeq() < head.request.getSeq()) {
            n.next = head;
            head = n;
            if (tail == null) tail = n;
        } else {
            Node cur = head;
            while (cur.next != null && cur.next.request.getSeq() < r.getSeq()) cur = cur.next;
            n.next = cur.next;
            cur.next = n;
            if (n.next == null) tail = n;
        }
        size++;
    }

    public List<Request> toList() {
        List<Request> out = new ArrayList<>();
        for (Node cur = head; cur != null; cur = cur.next) out.add(cur.request);
        return out;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
