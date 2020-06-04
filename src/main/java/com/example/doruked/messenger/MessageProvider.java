package com.example.doruked.messenger;

/**
 * This interface is capable of creating messages from received values and publishing that information to
 * those who request it.
 *
 * @implSpec Details regarding the management of messages are not specified by this interface. Such details
 * are determined by the implementation and should be documented. Here are some to consider:
 * (1) how long a message is stored (2) whether messages persist after being requested.
 *
 * If you are storing multiple messages consider {@link BulkMessageProvider<T>}
 *
 * @param <T> the type belong to the message
 */
public interface MessageProvider<T> extends MessageCreator<T> {

    /**
     * returns a message from this class.If no messages are present a documented value should be returned.
     * @return a messages from this class or a documented value if no messages are present
     */
    T send();
}
