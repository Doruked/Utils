package com.example.doruked;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class aims to provide an immutable {@code map}. Being immutable leaves the class unable implement {@link Map}
 * succinctly, so this class manually mirrors the methods it provides. This is fragile, but unlikely to matter given
 * that Map is unlikely to change much.
 * <p>
 * To retain the information that would be returned in normal {@link Map} methods, this class stores the would be
 * return value inside {@link ImmutableMap#result}. This information may be accessed by a call to {@link #getResult()}.
 * An example of it's usage is as followed: {@code this.remove(key).getResult()}
 * <p>
 * This class largely delegates it's behavior to a stored a {@code map}. For methods that would alter the stored map,
 * the behavior can be understood as follows:
 * 1.)An instruction that would modify the map has been requested
 * 2.) A defensive copy of the contained map is created
 * 3.) That copy is mutated according to the desired instruction.
 * 4.) A new ImmutableMap is returned containing the mutated map.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @param <R> the type returned by the map operation
 *            that produced this object
 * @implNote (1)This map is immutable, but the contents might not be. If invoking a method which publishes map contents,
 * this object may be modified in unaccounted ways. (2) When initializing this class through it's public constructor,
 * the underlying map will reflect the type passed in. However, on subsequent modifications (i.e. calls to
 * non-constructor methods that return a ImmutableMap) the map returned will be backed by a {@link HashMap}.
 */
public class ImmutableMap<K, V, R> {

    private R result;
    private final Map<K, V> map;

    private ImmutableMap(Map<K, V> map, R result) {
        this.map = map;
        this.result = result;
    }

    public ImmutableMap(Map<K, V> map) {
        this(map, null);
    }

    private ImmutableMap() {
        this(new HashMap<>(), null);
    }

    public R getResult() {
        return result;
    }

    /** @see Map#size() */
    public int size() {
        return map.size();
    }

    /** @see Map#isEmpty()  */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /** @see Map#containsKey(Object) */
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /** @see Map#containsValue(Object)*/
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * @implNote This map is immutable, but the contents might not be. If {@link V} is mutable, then publishing
     * this object may lead to unaccounted changes
     * @see Map#get(Object) for behavior
     */
    public V get(Object key) {
        return map.get(key);
    }

    /** @see Map#put(Object, Object) */
    public ImmutableMap<K, V, V> put(K key, V value) {
        return helperFunction(e -> map.put(key, value));
    }

    /** @see Map#remove(Object) */
    public ImmutableMap<K, V, V> remove(Object key) {
        return helperFunction(e -> map.remove(key));
    }

    /** @see Map#putAll(Map)  */
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        helperConsumer(e -> map.putAll(m));
    }

    /** @see Map#clear()  */
    public ImmutableMap<K, V, R> clear() {
        return ImmutableMap.emptyMap();
    }

    /**
     * @implNote This map is immutable, but the contents might not be. If {@link V} is mutable, then publishing
     * this set may lead to unaccounted changes
     * @see Map#keySet() for behavior
     */
    public Set<K> keySet() {
        return map.keySet();
    }

    /**
     * @implNote This map is immutable, but the contents might not be. If {@link V} is mutable, then publishing
     * this collection may lead to unaccounted changes
     * @see Map#values() for behavior
     */
    public Collection<V> values() {
        return map.values();
    }

    /**
     * @implNote This map is immutable, but the contents might not be. If {@link V} is mutable, then publishing
     * this set may lead to unaccounted changes
     * @see Map#entrySet() for behavior
     */
    public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    /** @see Map#getOrDefault(Object, Object)  */
    public ImmutableMap<K, V, V> getOrDefault(Object key, V defaultValue) {
        return helperFunction(e -> e.getOrDefault(key, defaultValue));
    }

    /** @see Map#forEach(BiConsumer)  */
    public void forEach(BiConsumer<? super K, ? super V> action) {
        helperConsumer(e -> e.forEach(action));
    }

    /** @see Map#replaceAll(BiFunction) */
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        helperConsumer(e -> replaceAll(function));
    }

    /** @see Map#putIfAbsent(Object, Object)  */
    public ImmutableMap<K, V, V> putIfAbsent(K key, V value) {
        return helperFunction(e -> e.putIfAbsent(key, value));
    }

    /** @see Map#remove(Object, Object)  */
    public ImmutableMap<K, V, Boolean> remove(Object key, Object value) {
        return helperFunction(e -> e.remove(key, value));
    }

    /** @see Map#replace(Object, Object, Object)  */
    public ImmutableMap<K, V, Boolean> replace(K key, V oldValue, V newValue) {
        return helperFunction(e -> e.replace(key, oldValue, newValue));
    }

    /** @see Map#replace(Object, Object)  */
    public ImmutableMap<K, V, V> replace(K key, V value) {
        return helperFunction(e -> e.replace(key, value));
    }

    /** @see Map#computeIfAbsent(Object, Function)  */
    public ImmutableMap<K, V, V> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return helperFunction(e -> e.computeIfAbsent(key, mappingFunction));
    }

    /** @see Map#computeIfAbsent(Object, Function) */
    public ImmutableMap<K, V, V> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return helperFunction(e -> e.computeIfPresent(key, remappingFunction));
    }

    /** @see Map#compute(Object, BiFunction)  */
    public ImmutableMap<K, V, V> compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return helperFunction(e -> e.compute(key, remappingFunction));
    }

    /** @see Map#merge(Object, Object, BiFunction)  */
    public ImmutableMap<K, V, V> merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return helperFunction(e -> e.merge(key, value, remappingFunction));
    }
//static

    /** returns an empty ImmutableMap */
    public static <K, V, R> ImmutableMap<K, V, R> emptyMap() {
        return new ImmutableMap<>();
    }

//helper

    private void setResult(R result) {
        this.result = result;
    }

    /**
     * Accepts a map consumer and returns a new ImmutableMap that matches the result of performing the desired
     * map consumer on this object's map.
     *
     * @param consumer a map operation that does not return a value
     * @param <T> ignored
     * @return an immutable map who's contents match the result of applying the
     *         consumer operation to this object's map
     */
    protected final <T> ImmutableMap<K, V, T> helperConsumer(Consumer<Map<K, V>> consumer) {
        Map<K, V> copy = copyMap(this.map);
        consumer.accept(copy);

        return new ImmutableMap<>(map);
    }

    /**
     * Accepts a map function and returns a new ImmutableMap that matches the result of performing the desired
     * map function on this object.
     *
     * Note: To use this method as a returnable value, the calling method must manually assign {@link T}
     * in it's return signature to match the return type of the used map function. If not, the compiler
     * will not infer the type. This a cost of writing one method vs many
     *
     * @param function a map operation that returns a value
     * @param <T> the type returned by the map function
     * @return an immutable map who's contents match the result of applying the
     *         function to this object's map
     */
    protected final <T> ImmutableMap<K, V, T> helperFunction(Function<Map<K, V>, T> function) {
        Map<K, V> copy = copyMap(this);
        T result = function.apply(copy);

        return new ImmutableMap<>(map, result);
    }

    /**
     *  creates a copy of the specified map. The purpose of this method is to allow specifying
     *  the default {@link Map} implementation that will backs this objects contained map.
     *
     * @param map the map to copy
     * @return a copy of the received map
     */
    protected Map<K,V> copyMap(Map<K,V> map){
        return new HashMap<>(map);
    }

    /**
     *  creates a copy of the specified map. The purpose of this method is to allow specifying
     *  the default {@link Map} implementation that will backs this objects contained map.
     *
     * @param map the ImmutableMap containing the map to copy
     * @return a copy of the parsed map
     */
    protected Map<K,V> copyMap(ImmutableMap<K,V,R> map){
        return copyMap(this.map);
    }
}
