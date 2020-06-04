package com.example.doruked.Junk;

import java.util.AbstractMap;

/**
 *
 * @deprecated  redundant with {@link com.example.doruked.Pair}
 * @param <K>
 * @param <V>
 */
@Deprecated
public class Pair<K,V>  implements AbstractMap.Entry<K,V>{

    private final AbstractMap.Entry<K,V> deleg;

    public Pair(AbstractMap.Entry<K, V> deleg) {
        this.deleg = deleg;
    }




    @Override
    public K getKey() {
        return null;
    }

    @Override
    public V getValue() {
        return null;
    }

    @Override
    public V setValue(V value) {
        return null;
    }


   public static <K,V> Pair<K,V> create(K key, V value){
        return new Pair<>(
                new AbstractMap.SimpleEntry<>(key,value));

    }
}
