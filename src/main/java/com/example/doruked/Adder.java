package com.example.doruked;

import java.util.ArrayList;
import java.util.List;

public class Adder<T> {

    private List<T> list;

    public Adder(List<T> list) {
        this.list = list;
    }

    public Adder() {
        list = new ArrayList<>();
    }

    public Adder<T> add(T obj) {
        list.add(obj);
        return this;
    }


    public List<T> getList() {
        return list;
    }

    public void erase() {
        list.clear();
    }


}
