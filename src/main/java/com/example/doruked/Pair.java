package com.example.doruked;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Just renames {@link AbstractMap.SimpleEntry} so it's not so unwieldy to use
 *
 * @see AbstractMap.SimpleEntry
 */


public class Pair<K,V> implements Map.Entry<K,V>, Serializable {

    private final AbstractMap.Entry<K, V> simpleEntry;

    /** Creates a {@code pair} representing the mapping of the specified key and value */
    public Pair(K key, V value) {
        simpleEntry = new AbstractMap.SimpleEntry<>(key, value);
    }

    /** Creates a {@code pair} representing the same mapping as the specified entry. */
    public Pair(Map.Entry<? extends K, ? extends V> entry) {
        simpleEntry = new AbstractMap.SimpleEntry<>(entry);
    }

    /** {@inheritDoc} */
    public K getKey() {
        return simpleEntry.getKey();
    }

    /** {@inheritDoc} */
    public V getValue() {
        return simpleEntry.getValue();
    }

    /** {@inheritDoc} */
    public V setValue(V value) {
        return simpleEntry.setValue(value);
    }

    /** {@inheritDoc} */
    public boolean equals(Object o) {
        return simpleEntry.equals(o);
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return simpleEntry.hashCode();
    }

    /** {@inheritDoc} */
    public String toString() {
        return simpleEntry.toString();
    }

    /** {@inheritDoc} */
    public static <K,V> Pair<K,V> of(K key, V value){
        return new Pair<>(key, value);
    }
}
