package com.example.doruked;

import java.util.Objects;

/**
 * This class provides methods that aid checking for null values.
 * This class is subject to grow in utility as needed.
 */
public class NullChecker {


    /**
     * Checks whether the specified {@code objects} are {@code null}.
     *
     * @param objects the objects to check for null
     * @throws NullPointerException if objects contains a null member
     */
    public static void notNull(Object... objects) {
        for (Object e : objects) {
            Objects.requireNonNull(e);
        }
    }
}
