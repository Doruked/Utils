package com.example.doruked.Responder;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * This class acts as a mediator between two classes who are communicating. The intended model is that there is a
 * "speaker" and a "responder". The speaker initiates communication and the responder may send it's response to the
 * speaker. The responder however does not initiate communication.
 * <p>
 * The subject of their communicate is contained within the {@code context} ({@link TContext}) which is parsed and interpreted by those
 * receiving it. When the responder responds they return a response ({@link TResponse}). The significance of this
 * response determined by the implementation.
 * <p>
 * The intended use is that it allows one class to send a packet of information ({@code context}. Some responder(s) then
 * views this information and returns a response to the received information. From here, the response is free to use.
 * <p>
 * Use Case:
 * When there is generated information {@code context} that may be require notifying another class
 * who would update/change the received information before it get's used by the sender.
 *
 * @param <TContext> the type of information handled by this mediator
 * @param <TResponse> the type returned after notification
 * @implNote this interface has several factories, that provide different implementations. Such as:
 * {@link #createBasic(Observer)}
 * {@link #createFutured(Observer)} (Observer)}
 * {@link #createFutureOne(Observer)} (Observer)}
 * {@link #createListed(Observer)} (Observer)}
 * {@link #creatUnary(Observer)} (Observer)}
 * @apiNote if the there is a desire to add or remove observers post construction
 *  see ({@link SlimPoller} and {@link Poller})
 */
public interface SlimMediator<TContext, TResponse> {

    /**
     * Notifies this object's observer(s) of the specified {@code context} and returns their response.
     * The number of observer and notification order are determined by an implementation.
     *
     * @param context the information to be passed to an observer(s)
     * @return the result of notification if it's immediately available, or null if waiting is required
     * @throws NullPointerException if context is null and null is not permitted
     * @throws InterruptedException if notification involves waiting on an interruptible observer
     */
    TResponse notify(TContext context) throws InterruptedException;

//extensions

    /**
     * Notification produces a single result of the same type as its operand
     *
     * @param <T> the type to notify of and retrieve
     */
    interface Unary<T> extends SlimMediator<T, T> {

        static <T> Unary<T> create(SlimMediator<T, T> delegate) {
            return delegate::notify;
        }
    }

    /**
     * Each notification produces multiple objects the notified type
     *
     * @param <T> the type to notify and recieve a list of
     */
    interface Listed<T> extends SlimMediator<T, List<T>> {

        static <T> Listed<T> create(SlimMediator<T, List<T>> delegate) {
            return delegate::notify;
        }
    }

    /**
     * Each notification generates a future {@code list}
     *
     * @param <T> the type to notify and recieve a future list of
     */
    interface Futured<T> extends SlimMediator<T, Future<List<T>>> {

        static <T> Futured<T> create(SlimMediator<T, Future<List<T>>> delegate) {
            return delegate::notify;
        }
    }

    /**
     * Each notification produces a single future containing
     * the same type as it's operand
     *
     * @param <T> the type to notify and recieve a future of
     */
    interface FutureOne<T> extends SlimMediator<T, Future<T>> {

        static <T> FutureOne<T> create(SlimMediator<T, Future<T>> delegate) {
            return delegate::notify;
        }
    }

//implementations

    /**
     * An immutable instance of {@link SlimMediator} with a single {@code observer}
     *
     * @param <TContext> the type of information handled by this mediator
     * @param <TResponse> the type returned after notification
     * @implSpec a single observer, simply means only one object is explicitly notified.
     * This is not to say, the {@code observer} cannot proliferate that message themselves.
     */
    class Basic<TContext, TResponse> implements SlimMediator<TContext,TResponse> {

        private final Observer<TContext, TResponse> observer;

        public Basic(Observer<TContext, TResponse> observer) {
            this.observer = observer;
        }

        /** {@inheritDoc} */
        @Override
        public TResponse notify(TContext context) throws InterruptedException {
            return observer.process(context);
        }

    }

//factories

    /**
     * Creates a new instance that delegates to the specifed {@code delegate}
     *
     * @param delegate the implementation to wrap
     * @param <E> the type of elements handled by the mediator
     * @return a new instance using the delegate
     */
    static <E> Unary<E> asUnary(SlimMediator<E, E> delegate) {
       return Unary.create(delegate);
    }

    /**
     * Creates a new instance, using the the specified {@code delegate}
     * for behavior
     *
     * @param delegate the implementation to wrap
     * @param <E> the type of elements handled by the mediator
     * @return a new instance using the delegate
     */
    static <E> Listed<E> asListed(SlimMediator<E, List<E>> delegate) {
        return Listed.create(delegate);
    }

    /**
     * Creates a new instance, using the the specified {@code delegate}
     * for behavior
     *
     * @param delegate the implementation to wrap
     * @param <E> the type of elements handled by the mediator
     * @return a new instance using the delegate
     */
    static <E> Futured<E> asFutured(SlimMediator<E, Future<List<E>>> delegate) {
        return Futured.create(delegate);
    }

    /**
     * Creates a new instance, using the the specified {@code delegate}
     * for behavior
     *
     * @param delegate the implementation to wrap
     * @param <E> the type of elements handled by the mediator
     * @return a new instance using the delegate
     */
    static <E> FutureOne<E> asFutureOne(SlimMediator<E, Future<E>> delegate) {
        return FutureOne.create(delegate);
    }


    /**
     * @param observer the observer to be contained
     * @param <T> the type accepted by observers
     * @param <R> the type returned by observers
     * @return a new instance
     */
    static <T, R> Basic<T, R> createBasic(Observer<T, R> observer) {
        return new Basic<>(observer);
    }

    /**
     * Creates new instance that delegates to {@link Basic}
     * for it's behavior
     *
     * @param observer the observer to contain
     * @param <T> the type of element used by observer
     * @return a new instance
     * @implSpec the type delegated to may change according to needs
     */
    static <T> Unary<T> creatUnary(Observer<T, T> observer) {
        return create(observer, Unary::create);
    }

    /**
     * Creates new instance that delegates to {@link Basic}
     * for it's behavior
     *
     * @param observer the observer to contain
     * @param <T> the type of element used by observer
     * @return a new instance
     * @implSpec the type delegated to may change according to needs
     */
    static <T> Listed<T> createListed(Observer<T, List<T>> observer) {
        return create(observer, Listed::create);
    }

    /**
     * Creates new instance that delegates to {@link Basic}
     * for it's behavior
     *
     * @param observer the observer to contain
     * @param <T> the type of element used by observer
     * @return a new instance
     * @implSpec the type delegated to may change according to needs
     */
    static <T> Futured<T> createFutured(Observer<T, Future<List<T>>> observer) {
        return create(observer, Futured::create);
    }

    /**
     * Creates new instance that delegates to {@link Basic}
     * for it's behavior
     *
     * @param observer the observer to contain
     * @param <T> the type of element used by observer
     * @return a new instance
     * @implSpec the type delegated to may change according to needs
     */
    static <T> FutureOne<T> createFutureOne(Observer<T, Future<T>> observer) {
        return create(observer, FutureOne::create);
    }

    static <TContext, TResponse, T extends SlimMediator<TContext, TResponse>> T create(Observer<TContext, TResponse> observer, Function<SlimMediator<TContext, TResponse>, T> builder) {
        SlimMediator<TContext, TResponse> delegate = createBasic(observer);
        return builder.apply(delegate);
    }

}
