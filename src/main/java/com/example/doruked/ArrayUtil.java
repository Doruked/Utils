package com.example.doruked;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ArrayUtil {


    //make a contains method

    /**
     * Returns a random member from the specified {@code values}. Each member should have
     * an equal chance of being chosen, and functions
     *
     * If the array is modified during the operation, results may not be as expected.
     *
     * @param values the values to choose from
     * @return a random member of the array or null if the array is empty
     * @throws NullPointerException if values is null
     */
    public static Object randomMember(Object[] values) {
        return randomMember(values, ThreadLocalRandom.current());
    }


    /**
     * Returns a random array member from the specified {@code values}. A member is chosen by generating an {@code index}
     * within the {@code array.length} and returning that member.
     * <p>
     * Number generation is done according to the specified {@link Random}
     *
     * @param values the values to choose from
     * @param rnd    the source of randoms
     * @return a random member of the array or null if the array is empty
     * @throws NullPointerException if values or source of randomness is null
     */
    public static Object randomMember(Object[] values, Random rnd){
        if(values == null || rnd == null) throw new NullPointerException();
        int size = values.length;
        if(size < 1) return null;
        else {
            int target = rnd.nextInt(size);
            return values[target];
        }
    }

}