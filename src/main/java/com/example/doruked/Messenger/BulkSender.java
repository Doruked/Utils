package com.example.doruked.Messenger;

import java.util.List;

/**
 * This interface is capable of creating messages from received values and publishing that information to
 * those who request it. This interface also assumes that it will handle, potentially, many messages
 * and sends it's stored data as a {@code list}.
 *
 * @implSpec Details regarding the management of messages are not specified by this interface. Such details
 * are determined by the implementation and should be documented. Here are some to consider:
 * (1) how long a message is stored (2) how many messages are stored (3) storage order
 * (4) whether messages persist after being requested
 * @param <T> the type belong to the message
 */
public interface BulkSender<T> extends MessageCreator<T> {

    /**
     * Returns a {@code list} of messages from this class. If no messages are present an empty list or documented
     * value should be returned.
     * @return a list of messages from this class or an empty list/ documented value if no messages are present
     */
    List<T> sendBulk();
}
