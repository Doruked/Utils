package com.example.doruked;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 * also converts other functional interfaces to a {@link Lambda}.
 * More will be added as needed as static methods named "build"
 */
public interface Lambda {

    void execute();


//factories

    /**
     * Creates a {@code lambda} that consists of applying
     * the specified {@code consumer} to the specified {@code object}
     *
     * @param object the object to consume
     * @param consumer the consumer action
     * @param <T> the type to consume
     * @return a lambda that executes the consumer on the object
     * @throws NullPointerException if consumer is null or the specified {@code object}
     * is null and the consumer doesn't accept null
     */
   static <T> Lambda build(T object, Consumer<T> consumer){
        return ()-> consumer.accept(object);
    }

    /**
     * Creates a {@code lambda} that consists of applying
     * the specified {@code consumer} to the specified {@code object}
     *
     * @param object1 the first input argument for the consumer
     * @param object2  the second input argument for the consumer
     * @param consumer the consumer action
     * @param <T> the type to consume
     * @return a lambda that executes the consumer on the objects
     * @throws NullPointerException if consumer is null or {@code object1/object2}
     * is null and the consumer doesn't accept null
     */
    static <T, U> Lambda build(T object1, U object2, BiConsumer<T, U> consumer){
        return ()-> consumer.accept(object1, object2);
    }

}
