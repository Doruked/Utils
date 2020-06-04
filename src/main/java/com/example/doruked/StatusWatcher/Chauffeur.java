package com.example.doruked.StatusWatcher;

public interface Chauffeur<T, R> {

    default void notify(T context) {
        throw new UnsupportedOperationException("This method is not implemented yet");
    }

    R getResponse();
}
