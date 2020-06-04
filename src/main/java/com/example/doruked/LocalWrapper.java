package com.example.doruked;


import java.util.function.UnaryOperator;

/**
 * An object wrapper.
 * It's intended use is as a local variable and being passed to nested methods who's behavior is known.
 * When used in such a way, this class imparts mutability on a variable of type {@link T} (who's type might
 * not be mutable), without the need of utilizing a return value after exiting various nested methods.
 *
 * In this way, LocalWrapper is used to obfuscate, hide, reduce code duplication or make certain code
 * "cleaner" if such a form is desired.
 *
 * Note: Passing this object to another method should entail knowing what the destination method does with this object.
 * Publishing may lead to unaccounted changes. If unknown, it may be best to unwrap the object or not use the class.
 *
 * @implNote  more methods may be added, but only for adding flexible ways of achieving the standard behaviors
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
