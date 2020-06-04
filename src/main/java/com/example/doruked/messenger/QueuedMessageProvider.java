package com.example.doruked.messenger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//when editing this documentation for clarity consider updating BulkMessageProvider who is fairly similar

/**
 * This class is capable of creating messages from received values and publishing that information to those who request.
 * Messages will be stored in bulk. And upon request, the oldest unretrieved message will be released and no longer stored.
 * This class will only place restrictions on the message capacity if {@link #QueuedMessageProvider(BlockingQueue)}}
 * was initialized to have them.
 * <p>
 * Thread Safety:
 * This class guarantees to be threadsafe in so far as a threadsafe implementation of {@link BlockingQueue} is used.
 * When using {@link #QueuedMessageProvider()} ()} this class guarantees to be threadsafe. In such cases the class
 * will be backed by a {@link LinkedBlockingQueue}. This is open to change for another viable alternative.
 *
 * @param <T> the type belonging to the message
 */
public class QueuedMessageProvider<T> implements MessageSender<T> {

    private final BlockingQueue<T> messages;

    /** Creates an instance of this class that uses the specified queue to store and provide messages */
    public QueuedMessageProvider(BlockingQueue<T> messages) {
        this.messages = messages;
    }

    /** Creates a threadsafe instance of this class */
    public QueuedMessageProvider() {
        this(new LinkedBlockingQueue<>());
    }

    /**
     * Returns the oldest unretrieved message stored by this class. If no messages exist on retrieval
     * or the method is interrupted, {@code null} is returned. In addition, when interrupted
     * that status will be proliferated.
     *
     * @return the oldest unretrieved message stored or null if empty or interrupted
     */
    @Override
    public T send() {
        try {
            return messages.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * This method accepts the specified {@code message} and stores it as is.
     * If messages have a capacity restriction the method will block until space is available.
     * In addition, if interrupted in this process that status will be proliferated.
     *
     * @param message the message to create and store
     */
    public void createMessage(T message) { //not entirely good, it will make sure it gets published but not that the other message isn't newer / already was used?
        try {
            messages.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
