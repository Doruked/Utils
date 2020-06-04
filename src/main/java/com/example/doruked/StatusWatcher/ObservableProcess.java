package com.example.doruked.StatusWatcher;

public interface ObservableProcess<T, R> extends Observable<R> {

    R process(T context);

}
