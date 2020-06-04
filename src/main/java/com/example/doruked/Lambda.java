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

//consumer
   static <T> Lambda build(T object, Consumer<T> consumer){
        return ()-> consumer.accept(object);
    }

    static <T, U> Lambda build(T object1, U object2, BiConsumer<T, U> consumer){
        return ()-> consumer.accept(object1, object2);
    }
////function //why would i convert function and not use it's value, in that case it should've been a consumer etc
//    static <T, U,R> Lambda build(T object1, U object2, BiFunction<T, U,R> function){
//        return ()-> function.apply(object1, object2);
//    }
//
//    static <T,R> Lambda build(T object1, Function<T,R> function){
//        return ()-> function.apply(object1);
//    }

}
