package com.example.doruked;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.junit.Assert.*;

public class ListUtilTest {


    @Test
    public void test_getSome_returns_expected_result() {
        Predicate<Integer> whenEquals3 = e-> e.equals(3);
        UnaryOperator<Integer> getPrevious = i -> i -1;

        List<Integer> list = List.of(1, 2, 3, 4, 5);

        Integer some = ListUtil.getSome(list, whenEquals3, getPrevious);
        assertEquals(2, some.intValue());
    }

    @Test
    public void test_getSome_returns_null_instead_of_throwing_IndexOutOfBounds_when_too_small() {
        helperTest_getSome_does_not_throw_IndexOutOfBounds((list, i) -> i - list.size());
    }

    @Test
    public void test_getSome_returns_null_instead_of_throwing_IndexOutOfBounds_when_too_big() {
        helperTest_getSome_does_not_throw_IndexOutOfBounds((list, i) -> i + list.size());
    }

    private void helperTest_getSome_does_not_throw_IndexOutOfBounds(BiFunction<List<?>,Integer, Integer> function){
        List<Integer> list = List.of(1, 2, 3, 4, 5);
        Predicate<Integer> first = e-> true;
        UnaryOperator<Integer> indexTransformation = i-> function.apply(list, i);

        Integer some = ListUtil.getSome(list, first, indexTransformation);

        assertNull(some);
    }

    @Test
    public void test_duplicateIndex_does_not_throw_IndexOutOfBounds_when_targeting_last_index(){
        Integer[] ar = {1,2,3,4,5};
        List<Integer> list = new ArrayList<>(Arrays.asList(ar)) ;

        UnaryOperator<Integer> copy = i-> i;
        try {
            ListUtil.duplicateIndex(list, list.size() - 1, copy);
        } catch (IndexOutOfBoundsException e){
            fail("index out of bounds");
        }
    }

}