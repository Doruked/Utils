package com.example.doruked;


import java.util.function.UnaryOperator;

/**
 * An object wrapper used to somewhat emulate "passing by reference" in the java language.
 * It is emulated by modifying the internal object when being passed.
 *
 * @apiNote It's intended use is as a local variable and being passed to nested methods.
 * The goal would be making some internal code cleaner/reducing duplication, not a general strategy.
 * @implNote more methods may be added, but only for adding flexible ways of achieving the standard behaviors
 * which are to "set" and "get"
 */
public final class LocalWrapper<T> {

    private T object;

    public LocalWrapper(T object) {
        this.object = object;
    }

   public T get() {
        return object;
    }

    public void set(T object) {
        this.object = object;
    }

    public void set(UnaryOperator<T> unary){
        this.object = unary.apply(object);
    }
}
