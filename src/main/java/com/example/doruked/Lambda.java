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

   static <T> Lambda build(T object, Consumer<T> consumer){
        return ()-> consumer.accept(object);
    }

    static <T, U> Lambda build(T object1, U object2, BiConsumer<T, U> consumer){
        return ()-> consumer.accept(object1, object2);
    }

}
