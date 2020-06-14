package com.example.doruked;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * This class contains a growing list of {@link List} utilities. The public methods added will,
 * in all likely-hood, be {@code static} only.
 */
public final class ListUtil {


//Create

    /**
     * Creates a {@link List} of {@link T} that is the result of filtering the specified {@code collection}
     * using the specified {@code pred}. When an object matches the {@code pred}, that object is appended to the
     * returned {@code list}, in the order it was encountered (i.e. the order of the specified {@code collection}).
     *
     * @param pred the predicate that if matched, adds the object
     * @param collection the collection to filter
     * @param <T> the type to be added
     * @return a list of the objects from the specified collection that match the predicate
     * @throws NullPointerException if predicate or collection is null
     */
    public static <T> List<T> create(Predicate<? super T> pred, Collection<T> collection){
        List<T> list = new ArrayList<>();
        for(T t:  collection){
            if(pred.test(t)){
                list.add(t);
            }
        }
        return list;
    }

    /**
     * Remaps the specified {@code values} based on the specified {@code function}. The order of the mapping
     * is equivalent to the order of the specified {@code values}.
     * <p>
     * This method does not modify the specified {@code values} but rather returns a new object.
     *
     * @param values   the values to unbox/ remap
     * @param function the function to apply to each element
     * @param <T>      the type being remapped
     * @param <R>      the new elements
     * @return a list containing elements that are the result of applying the specified function to
     * each element within the specified values
     * @implSpec This method is similar to already existing behavior such as {@link Stream#map(Function)}, with the
     * downside that you cannot chain additional behavior. If no additional behavior is needed this may be preferred.
     * You may also use {@link List#forEach(Consumer)} if the result isn't explicitly needed.
     * @throws NullPointerException if function or collection is null
     */
    public static <T, R> List<R> unbox(Collection<? extends T> values, Function<? super T, ? extends R> function) {
        List<R> list = new ArrayList<>();
        for (T t : values) {
            R result = function.apply(t);
            list.add(result);
        }
        return list;
    }

//Query

    /**
     * Returns whether the specified {@code list} is not {@code null} or empty.
     *
     * @param list the list to check
     * @return true if empty or null, false if not
     */
    public static boolean emptyOrNull(List<?> list) {
        return (list == null) || list.size() < 1;
    }

    /**
     * Returns whether the specified {@code list} is not {@code null} or empty.
     *
     * @param list the list to check
     * @return true if the list contains members, false if not
     */
    public static boolean notEmptyOrNull(List<?> list) {
        return !emptyOrNull(list);
    }

    /**
     * Checks the specified {@code collection} for an element that shares
     * referential equality with the specified {@code reference}
     *
     * @param collection the collection to search.
     * @param reference the sought after reference
     * @param <T> the type of elements contained by collection
     * @return true if an element
     * @throws NullPointerException if collection or reference is null
     */
    public static <T> boolean containsReference(Collection<? extends T> collection, T reference){
        for(T e: collection){
            if(reference == e)
                return true;
        }
        return false;
    }

//Get Element

    /**
     * Returns a new collection consisting of the elements that matched the specified {@code predicate}
     * when applied to the specified {@code collection}.
     *
     * @param pred the filter to apply
     * @param collection the collection to filter
     * @param <T> the type to filter
     * @return a collection consisting of the elements in the specified collection that matched the predicate
     * @throws NullPointerException if predicate or collection is null
     */
    public static <T> List<T> getIf(Predicate<? super T> pred, Collection<? extends T> collection) {
        List<T> filtered = new ArrayList<>();
        for (T t : collection) {
            if (pred.test(t))
                filtered.add(t);
        }
        return filtered;
    }

    /**
     * Returns the first element encountered in the specified {@code collection} that matches
     * the specified {@code predicate}.
     *
     * @param pred the predicate to match
     * @param collection the list to search
     * @param <T> the type to be removed
     * @return the first element to match the predicate or null
     * @throws NullPointerException if predicate or collection is null
     */
    public static <T> T get(Predicate<? super T> pred, Collection<? extends T> collection) {
        for (T e : collection) {
            if (pred.test(e))
                return e;
        }
        return null;
    }

    /**
     * Searches the specified {@code list} for the element that occurs at {@code index +1}
     * of the {@code list member} that equals the specified {@code target}
     *
     * @param list   the list to get the element from
     * @param target the element to get the next of
     * @param <T>    the type of elements to search and return
     * @return the next element or null if doesn't exist
     * @implSpec if multiple elements equal the specified {@code target}, the first is used.
     */
    public static <T> T getNextFrom(List<T> list, T target){
        return getSomeWhenEquals(list, target, i -> i +1);
    }

    /**
     * Searches the specified {@code list} for the element that occurs at {@code index +1}
     * of the {@code list member} that shares the same reference as the specified {@code reference}
     *
     * @param list the list to get the element from
     * @param reference the reference to get the next of
     * @param <T> the type of elements to search and return
     * @return the next element or null if doesn't exist
     */
    public static <T> T getNextFromReference(List<T> list, T reference){
        return getSomeWhenReference(list, reference, i -> i +1);
    }

    /**
     * Searches the specified {@code list} for the element that occurs at {@code index -1}
     * of the {@code list member} that equals the specified {@code target}
     *
     * @param list the list to get the element from
     * @param target the element to get the previous of
     * @param <T> the type of elements to search and return
     * @return the previous element or null if doesn't exist
     */
    public static <T> T getPreviousFrom(List<T> list, T target){
        return getSomeWhenEquals(list, target, i -> i - 1);
    }

    /**
     * Searches the specified {@code list} for the element that occurs at {@code index -1}
     * of the {@code list member} that shares the same reference as the specified {@code reference}
     *
     * @param list the list to get the element from
     * @param reference the reference to get the previous of
     * @param <T> the type of elements to search and return
     * @return the previous element or null if doesn't exist
     */
    public static <T> T getPreviousFromReference(List<T> list, T reference){
        return getSomeWhenReference(list, reference, i -> i - 1);
    }

    /**
     * Searches the specified {@code list} for an element retrievable by the following steps:
     * (1)Retrieves the element in the specified {@code list} that matches the specified {@code predicate},
     * (2)then applies the specified {@code indexTransformation} to it's index. The element at the resulting
     * index is returned.
     *
     * @param list the list of elements to search
     * @param pred the predicate matching the element to get the index of
     * @param indexTransformation the transformation to apply to the found index
     * @param <T> the type of elements to search and return
     * @return the retrieved element or null if not found
     * @implSpec if multiple members match the predicate, the first is used
     */
    public static <T> T getSome(List<T> list, Predicate<T> pred, UnaryOperator<Integer> indexTransformation) {
        int i = getIndex(list, pred);
        Integer targetIndex = indexTransformation.apply(i);

        if (targetIndex < list.size() && targetIndex > -1) {
            return list.get(targetIndex);
        }
        return null;
    }

//Get Index

    /**
     * Returns the index of the {@code element} in the specified {@code list} that is referentially
     * equal to the specified {@code reference}. If no match is found {@code -1} is returned.
     *
     * @param list the list to search
     * @param reference the reference to find the index of
     * @param <T> the type contained by list
     * @return the index of the element that is referentially equal to the specified reference or -1 if not found
     */
    public static <T> int getReferenceIndex(List<? extends T> list, T reference){
        for(int i = 0; i < list.size(); i++){
            if(reference == list.get(i)){
                return i;
            }
        }
        return -1;
    }

    /**
     * Retrieves the first index in the specified {@code list} that matches
     * the specified {@code predicate}, or {@code -1} if none match
     *
     * @param list the list of elements to apply the predicate to
     * @param pred the predicate to match
     * @param <T> the types to test
     * @return the index that matched the predicate or {@code -1} if none match
     * @implSpec results in some duplicate code in {@link #getReferenceIndex(List, Object)}
     */
    public static <T> int getIndex(List<T> list, Predicate<T> pred){
        for(int i = 0; i< list.size(); i++){
            if(pred.test(list.get(i))){
                return i;
            }
        }
        return -1;
    }

//Remove Element(s)

    /**
     * Returns the first element encountered in the specified {@code list} that matches
     * the specified {@code predicate}.
     *
     * @param pred the predicate to match
     * @param collection the list to search
     * @param <T> the type to be removed
     * @return the removable first element to match the predicate or null
     * @throws NullPointerException if predicate or collection is null
     */
    public static <T> T remove(Predicate<? super T> pred, Collection<? extends T> collection) {
        for (T e : collection) {
            if (pred.test(e))
                if(collection.remove(e))
                    return e;
        }
        return null;
    }

    /**
     * Removes all elements in the specified {@code collection} that match the specified {@code predicate}.
     * The removed elements are returned or if none exists, an empty {@code list}.
     *
     * @param pred       the predicate to match
     * @param collection the collection to filter
     * @param <T>        the type removed
     * @return the elements that matched the predicate and were remove or an empty list
     * @throws NullPointerException if predicate or collection is null
     * @implSpec consider using {@link List#removeIf(Predicate)}. The use of this method is that
     * it returns the elements that are removed.
     */
    public static <T> List<T> removeIf(Predicate<? super T> pred, Collection<? extends T> collection) {
        List<T> removed = new ArrayList<>();
        for (T e : collection) {
            if (pred.test(e))
                if (collection.remove(e))
                    removed.add(e);
        }
        return removed;
    }

    /**
     * Removes from the specified {@code list} the elements that occur
     * after the specified {@code bound}
     *
     * @param list  the list to remove elements from
     * @param bound the object who's index is the bound (inclusive)
     * @param <T>   the type of elements contained by list
     * @return the removed elements
     * @throws NullPointerException if list is null
     * @implSpec if the {@code bound} occurs multiple times in the {@code list},
     * the first occurrence is used.
     */
    public static <T> List<T> trimTo(List<T> list, T bound){
        int i = list.indexOf(bound);
        return trimToIndex(list,i);
    }

    /**
     * Removes from the specified {@code list} the elements that occur
     * after the specified {@code reference}
     *
     * @param list  the list to remove elements from
     * @param reference the object who's index is the bound (inclusive)
     * @param <T>   the type of elements contained by list
     * @return the removed elements
     * @throws NullPointerException if list is null
     * @implSpec if the {@code bound} occurs multiple times in the {@code list},
     * the first occurrence is used.
     */
    public static <T> List<T> trimToReference(List<T> list, T reference){
        int i = ListUtil.getReferenceIndex(list, reference);
        return trimToIndex(list,i);
    }

    /**
     * Removes from the specified {@code list} the elements that occur
     * after the specified {@code bound}
     *
     * @param list the list to remove elements from
     * @param bound the inclusive bound
     * @param <T> the type of elements contained by list
     * @return the removed elements
     * @throws NullPointerException if list is null
     * @throws IndexOutOfBoundsException
     */
    public static <T> List<T> trimToIndex(List<T> list, int bound){
       if(bound < 0 || bound > list.size()) throw new IndexOutOfBoundsException();
        List<T> removed = new ArrayList<>();
        while (bound != list.size() - 1) {
            T e = list.remove(list.size() - 1);
            removed.add(e);
        }
        return removed;
    }

//Add

    /**
     * Inserts the element at the specified {@code index} again at that position.
     *
     * @param list the list to duplicate from and to
     * @param index the index to duplicate
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index > size()})
     * @implNote this does not add a new object, but rather the same one will be
     * present twice.
     * @throws UnsupportedOperationException if the {@code add} operation
     *         is not supported by the specified list
     */
    public static<T> void duplicateIndex(List<T> list, int index){
        T reAdd = list.get(index);
        list.add(index, reAdd);
    }

    /**
     * Retrieves the element at the specified {@code index} and applies
     * the specified {@code copyFunction} to it. The result of that function
     * is added at {@code index + 1}. Note: this means the duplicate should occur
     * after the original.
     *
     * Inserts the element at the specified {@code index} again at that position.
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index > size()})
     * @throws UnsupportedOperationException if the {@code add} operation
     *         is not supported by the specified list
     * @param copyFunction a function that should produce a copy of it's operand
     */
    public static <T> void duplicateIndex(List<T> list, int index, UnaryOperator<T> copyFunction) {
        T reAdd = list.get(index);
        T copy = copyFunction.apply(reAdd);
        list.add(index + 1, copy);
    }

    /**
     * Retrieves the object provided by the specified {@code sup} and appends that object
     * to the specified{@code list} until the amount appended equals the specified {@code amount}
     * Note: this method mutates the specified list
     * <p>
     * if {@code amount < 1} no objects are added and the provided list is simply returne
     *
     * @param list the list to add to
     * @param amount the amount to add
     * @param sup the function that produces an object to add
     * @param <T> the type to be contained
     * @return a list that is the result of appending the requested amount
     *         of the supplied object to the specified list
     * @implNote the method mutates the specified {@code list}
     * @throws NullPointerException if the list or supplier is null
     */
    public static <T> List<T> addSupplied(List<T> list, int amount, Supplier<? extends T> sup){
       return addAmount(list,amount, sup.get());
    }

    /**
     * Retrieves the object provided by the specified {@code sup} and appends that object
     * to the specified{@code list} until the amount appended equals the specified {@code amount}
     * Note: this method mutates the specified list
     * <p>
     * if {@code amount < 1} no objects are added and the provided list is simply returned.
     *
     * This method optionally reevaluates the {@code sup} on each add when {@code reevaluate == true}.
     * Meaning, if the {@code supplier} produces potentially different results on each call, that potentiality is
     * reflected in the adds that occur.
     *
     * @param list       the list to add to
     * @param amount     the amount to add
     * @param sup        the function that produces an object to add
     * @param reevaluate whether the supplier is re-invoked for each add
     * @param <T>        the type to be contained
     * @return a list that is the result of appending the requested amount
     *         of the supplied object to the specified list
     * @throws NullPointerException if (1) the list is null (2) the supplier is null
     * @throws IllegalArgumentException if amount < 0
     * @implNote exceptions thrown in {@link List#add(Object)} apply to this method
     * @throws NullPointerException if the list or supplier is null
     */
    public static <T> List<T> addSupplied(List<T> list, int amount, Supplier<? extends T> sup, boolean reevaluate){
        if(!reevaluate)return addSupplied(list,amount,sup);
        else {
            if (helperCheckAmount(amount)) {
                for (int i = 0; i < amount; i++) {
                    list.add(sup.get());
                }
            }
        }
        return list;
    }

    /**
     * Appends the specified {@code object} to the specified {@code list} until the amount
     * appended equals the specified {@code amount}.
     *
     * if {@code amount < 0} no objects are added and the provided list is simply returned.
     *
     * @param list the list to add to
     * @param amount the amount to add
     * @param object the object to add
     * @param <T> the type to be contained
     * @return a list that is the result of appending the requested amount
     *         of the supplied object to the specified list
     * @throws NullPointerException if (1) the list is null (2) the object is null and not permitted by the list
     * @throws IllegalArgumentException if amount < 0
     * @implNote exceptions thrown in {@link List#add(Object)} apply to this method
     * @throws NullPointerException if the list or supplier is null
     */
    public static <T> List<T> addAmount(List<T> list, int amount, T object){
       if(helperCheckAmount(amount)) {
           for (int i = 0; i < amount; i++) {
               list.add(object);
           }
       }
       return list;
    }

//General

    /**
     * Checks the specified {@code collection} for member that matches the specified {@code pred}.
     * Once found, the specified {@code action} is applied to the member and the result is returned.
     *
     * @param collection the collection to search
     * @param pred the condition that decides if the action is done
     * @param action the action to apply
     * @param <T> the type contained the collection and input for some action
     * @param <R> the output of some action
     * @return the result of applying the action to the collection member that matched the predicate
     * @implNote the action is applied to the first member to match the predicate
     * @deprecated not sure of the use of this method. Deprecated until I find I would use it
     */
    @Deprecated
    public static <T, R> R doOnceIf(Collection<? extends T> collection, Predicate<? super T> pred, Function<T,R> action){
        for(T e: collection){
            if (pred.test(e)){
                return action.apply(e);
            }
        }
        return null;
    }

//Helpers

    private static boolean helperCheckAmount(int amount){
        if(amount < 0) throw new IllegalArgumentException("Provided amount: " + amount + " is less than 0");
        else return true;
    }

    private static <T> T getSomeWhenReference(List<T> list, T reference, UnaryOperator<Integer> indexTransformation) {
        return getSome(list, e-> e == reference, indexTransformation);
    }

    private static <T> T getSomeWhenEquals(List<T> list, T target, UnaryOperator<Integer> indexTransformation) {
        return getSome(list, e-> e.equals(target), indexTransformation);
    }
}
