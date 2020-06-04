package com.example.doruked.statuswatcher;

public interface Chauffeur<T, R> {

    default void notify(T context) {
        throw new UnsupportedOperationException("This method is not implemented yet");
    }

    R getResponse();
}
