package trspell.corpus;

import org.jmate.Comparators;
import static org.jmate.Preconditions.checkArgument;
import static org.jmate.Preconditions.checkNotNull;

import java.util.*;

/**
 * a simple set like data structure for counting elements. not thread safe.
 * Tr: amele bir sayacli kume veri yapisi. icine koyulan her elemanla birlikte o elemana iliskin bir sayaci da arttirir.
 */
public class CountingSet<T> implements Iterable<T> {

    private final Map<T, Integer> map = new HashMap<T, Integer>();

    /**
     * adds an element. and increments it's count.
     *
     * @param t element to add.
     * @return the count of the added element.
     * @throws NullPointerException if element is null.
     */
    public int add(T t) {
        checkNotNull(t, "Element cannot be null");
        if (map.containsKey(t)) {
            int j = map.get(t) + 1;
            map.put(t, j);
            return j;
        } else {
            map.put(t, 1);
            return 1;
        }
    }

    /**
     * returns the total element count of the counting set.
     *
     * @return element count.
     */
    public int size() {
        return map.size();
    }

    /**
     * merges another CountingSet to this one.
     *
     * @param otherSet another CountingSet
     */
    public void merge(CountingSet<T> otherSet) {
        checkNotNull(otherSet, "CountingSet cannot be null");
        for (T t : otherSet) {
            merge(t, otherSet.getCount(t));
        }
    }

    /**
     * inserts the element and its value. it overrides the current count
     *
     * @param t element
     * @param c count value which will override the current count value.
     */
    public void insert(T t, int c) {
        checkNotNull(t, "Element cannot be null");
        checkArgument(c >= 0, "Element count cannot be negative.");
        map.put(t, c);
    }

    /**
     * it merges the element and it's count with the current value.
     *
     * @param t element
     * @param c it's count to merge.
     */
    public void merge(T t, int c) {
        checkNotNull(t, "Element cannot be null");
        checkArgument(c >= 0, "Element count cannot be negative.");
        if (map.containsKey(t)) {
            map.put(t, map.get(t) + c);
        } else
            map.put(t, c);
    }

    /**
     * adds a collection of elements.
     *
     * @param collection a collection of elements.
     */
    public void add(Collection<T> collection) {
        checkNotNull(collection, "CountingSet cannot be null");
        for (T t : collection) {
            add(t);
        }
    }

    /**
     * current count of the given element
     *
     * @param t element
     * @return count of the element. if element does not exist, 0
     */
    public int getCount(T t) {
        if (map.containsKey(t))
            return map.get(t);
        else return 0;
    }

    /**
     * if element exist.
     *
     * @param t element.
     * @return if element exists.
     */
    public boolean contains(T t) {
        return map.containsKey(t);
    }

    /**
     * returns a Pair<T> list containing elemetns and it's counts.
     * sorted with the count of the elements descending.
     *
     * @return a Pair<T> list, sorted by count, descending
     */
    public List<Pair<T>> getSortedPairList() {
        List<Pair<T>> l = new ArrayList<Pair<T>>();
        for (Map.Entry<T, Integer> entry : map.entrySet()) {
            l.add(new Pair<T>(entry.getKey(), entry.getValue()));
        }
        Collections.sort(l);
        return l;
    }

    public List<T> getFirst(int count) {
        if (count > this.size())
            count = this.size();
        return getSortedList().subList(0, count);
    }

    /**
     * returns the Elements in a list sorted by count, descending..
     *
     * @return Elements in a list sorted by count, descending..
     */
    public List<T> getSortedList() {
        List<Pair<T>> l = getSortedPairList();
        List<T> lst = new ArrayList<T>();
        for (Pair<T> p : l) {
            lst.add(p.t);
        }
        return lst;
    }

    /**
     * returns the Elements in a list sorted by the given comparator..
     *
     * @param comp a Comarator of T
     * @return Elements in a list sorted by the given comparator..
     */
    public List<T> getSortedList(Comparator<T> comp) {
        List<T> l = new ArrayList<T>(getSet());
        Collections.sort(l, comp);
        return l;
    }

    /**
     * returns elements in a set.
     *
     * @return a set containing the elements.
     */
    public Set<T> getSet() {
        return map.keySet();
    }

    /**
     * returns an iterator for elements.
     *
     * @return returns an iterator for elements.
     */
    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }


    static class Pair<T> implements Comparable<Pair<T>> {
        final T t;
        final int i;

        Pair(T t, int i) {
            this.t = t;
            this.i = i;
        }

        T object() {
            return t;
        }

        int count() {
            return i;
        }

        public int compareTo(Pair<T> pair) {
            return Comparators.compare(pair.i, this.i);
        }
    }
}
