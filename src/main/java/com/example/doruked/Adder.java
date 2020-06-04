package com.example.doruked;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated could be improved, such as implementing {@link List}.
 * The utility of this seems minimal and I don't have a particular use.
 * At most it provides syntactic shortcuts to {@code add} operations.
 * @param <T>
 */
@Deprecated
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
