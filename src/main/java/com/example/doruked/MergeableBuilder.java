package com.example.doruked;


/**
 * This class helps create builder classes that are mergeable with the {@code type} they intend to build.
 * The act of merging is a convenience operation that allows a builder to specify only a few values then
 * merge with an existing object to fill the rest.
 *
 * Aside from a normal builder operations, there are only two extra methods to implement. And both only
 * require a trivial implementation. It is not planned for this class to add more methods that require implementation.
 *
 * @param <T> the type that will be built
 */
public abstract class MergeableBuilder<T> {


    /**
     * Returns a constructed instance of {@link T} using the values stored by this object.
     *
     * @return a {@link T} constructed from this object's values.
     */
    public abstract T build();


    /**
     * This method merges with the specified {@code object} and returns the result. In simple terms, a merge is where
     * a builder extracts values from some container. This allows a builder to specify only a few values then
     * merge with an existing object to fill the rest.
     *
     * @param object the object to extract values from.
     * @return a builder that has merged this object with the specified object
     * @implSpec  When this method merges, it should only replace existing values if they're {@code null}. If this is
     * not guaranteed, this may lead to {@link #merge(Object, boolean)} not behaving as expected.
     *
     * An implementation of this method can be nearly the same for any extending class. However, in that this class
     * does not know the {@code fields} that would be used, a default implementation cannot be provided.
     * <p>
     * To Implement this Method: just call {@link #helperSort(Object, Object)} on all mergeable field. The result
     * should like like {@code field = helperSort(this.value, object.value)}
     */
    public abstract MergeableBuilder<T> merge(T object);

    /**
     * This method merges with the specified {@code object} with the option to specify different behavior
     * for how the merger occurs. If {@code overwrite} is true, the values within the specified {@code object}
     * are used when two {@code non-null} values are eligible to be contained. If false, it is only used when
     * replacing a null value.
     * <p>
     * In simple terms, a merge is where a builder extracts values from some container. This allows a builder to
     * specify only a few values then merge with an existing object to fill the rest.
     *
     * @param object    the object to merge with
     * @param overwrite whether the specified object should overwrite this objects non-null values
     * @return a builder that has merged this object with the specified object
     * @throws NullPointerException if object is null
     */
    public MergeableBuilder<T> merge(T object, boolean overwrite){
        if(overwrite) return createEmpty().merge(object).merge(this.build());
        else return this.merge(object);
    }

    /**
     * Merges with specified {@code object} and builds the result.
     * @see #merge(Object)
     * @see #build(); 
     * 
     * @param object the object to merge with
     * @return an instance of {@link T} that is the result of merging with the specified {@code object}
     */
    public T mergeBuild(T object) {
        return merge(object).build();
    }
    /**
     * Merges with specified {@code object} with the option to specify different behavior for how the merger occurs.
     * The resulting merge is then built into an instance of {@link T} and returned.
     * 
     * @see #merge(Object, boolean)
     * @see #build(); 
     *
     * @param object the object to merge with
     * @param overwrite whether the specified object should overwrite this objects non-null values       
     * @return an instance of {@link T} that is the result of merging with the specified {@code object}
     */
    public T mergeBuild(T object, boolean overwrite){
        return merge(object, overwrite).build();
    }


//helpers

    /**
     * Returns an empty instance of this builder. This is used to help implement a more complex method,
     * and make extending this class easier.
     *
     * @return an empty instance of this class
     */
    protected abstract MergeableBuilder<T> createEmpty();


    /**
     * This method chooses between two values and returns it's choice. A value is preferred when it is {@code non-null},
     * and if both {@code parameters} meet that criteria, {@code self} is prioritized.
     *
     * @param self  the value contained in this object
     * @param other the value from another object
     * @param <U>   the object to compare/sort
     * @return
     * @implNote the intention of this method is to make it easier to implement {@link #merge(Object)}.
     * In that situation, you would call this method on all mergeable field. The resulting code should look like
     * {@code field = helperSort(this.value, object.value)}
     */
    protected <U> U helperSort(U self, U other) {
        return self != null ? self : other;
    }
}
