package com.example.doruked.StatusWatcher;

public class SimpleChauffeur<T, R> implements Chauffeur<T, R> {

    private ObservableProcess<T, R> observable;

    public SimpleChauffeur(ObservableProcess<T, R> observable) {
        this.observable = observable;
    }

    @Override
    public R getResponse() {
        return observable.getStatus();
    }
}
