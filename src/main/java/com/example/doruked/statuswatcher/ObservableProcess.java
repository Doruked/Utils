package com.example.doruked.statuswatcher;

public interface ObservableProcess<T, R> extends Observable<R> {

    R process(T context);

}
