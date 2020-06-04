package com.example.doruked.Messenger;

import java.util.function.Supplier;

/**
 * This class is used to create objects viewed as "messages".
 *
 * @implSpec this interface does not enforce or require that these messages be stored
 *           or made available through this interface.
 * @param <T> the type belonging to the message
 */
public interface MessageCreator<T> {

    /**
     * This method creates a message from the specified {@code value}.
     * @param value the value to create a message from.
     * @throws NullPointerException if value is null and null is not accepted
     */
    void createMessage(T value);

    /**
     * This method creates a message from the specified {@code supplier}.
     *
     * @implSpec This method is delegates to {@link #createMessage(Object)} for behavior. When overriding
     * this method it may lead varying creation behavior for methods in this class. That might not be intuitive to users.
     * @param supplier the message supplier
     * @throws NullPointerException if supplier is null and null is not accepted
     */
    default void createMessage(Supplier<T> supplier) {
        createMessage(supplier.get());
    }
}
