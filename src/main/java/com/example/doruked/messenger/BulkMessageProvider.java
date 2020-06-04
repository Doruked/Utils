package com.example.doruked.messenger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

//when editing this documentation for clarity consider updating QueuedMessageProvider who is fairly similar

/**
 * This class is capable of creating messages from received values and publishing that information to those who request.
 * Messages will be stored in bulk. And upon request, all stored message will be released and no longer stored.
 * This class will only place restrictions on the message capacity if {@link #BulkMessageProvider(BlockingQueue)}
 * was initialized to have them.
 * <p>
 * Thread Safety:
 * This class guarantees to be threadsafe in so far as a threadsafe implementation of {@link BlockingQueue} is used.
 * When using {@link #BulkMessageProvider()} this class guarantees to be threadsafe. In such cases the class
 * will be backed by a {@link LinkedBlockingQueue}. This is open to change for another viable alternative.
 *
 * @param <T> the type belonging to the message
 * @deprecated this package seems to be redundant with the {@link com.example.doruked.responder} package.
 * Further, the generics may need to change to allow the sent type to be different from the
 * received type for this to be of good use.
 */
@Deprecated
public class BulkMessageProvider<T> implements BulkProvider<T> {

    private final BlockingQueue<T> bulk;

    /** Creates an instance of this class that uses the specified queue to store and provide messages */
    public BulkMessageProvider(BlockingQueue<T> queue) {
        this.bulk = queue;
    }

    /** Creates a threadsafe instance of this class */
    public BulkMessageProvider() {
        this(new LinkedBlockingQueue<>());
    }

    /**
     * Returns all messages stored by this class. If no messages are present,
     * an empty {@code list} is returned.
     *
     * @return all messages stored by this class or an empty list if no messages exist.
     */
    @Override
    public List<T> sendBulk() {
        List<T> send = new ArrayList<>();
        bulk.drainTo(send);
        return send;
    }

    /**
     * This method accepts the specified {@code message} and stores it as is.
     * Note: timing may cause some messages to be stored a little later than others.
     *
     * @param message the message to create
     * @throws NullPointerException if message is null
     * @throws IllegalStateException if the messages have a capacity restriction and is full
     */
    public void createMessage(T message) {
        if(message == null) throw new NullPointerException("Received message is null");
        bulk.add(message);
    }

    @Override
    public T send() {
        throw new UnsupportedOperationException("This method is not implemented yet");
    }

    @Override
    public void createMessage(Supplier<T> supplier) {
        throw new UnsupportedOperationException("This method is not implemented yet");
    }
}
