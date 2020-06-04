package com.example.doruked.responder;

import com.example.doruked.exceptions.InterruptedExceptionStamped;

import java.util.*;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

/**
 * This interface is a {@link SlimPoller} that may add and remove observers to notify.
 * The only reason it does not extend {@code SlimPoller} is that it leads to tricky inheritance.
 * <p>
 * Of note, having multiple observers means that each call to notify is expected to produce
 * a {@code collection/list} of responses.
 *
 * @param <TContext>  the type of information notified by this poller
 * @param <TResponse> the type returned after notification
 * @param <TObserver> the type of observers notified by this poller
 * @implNote this interface has several factories, providing different implementations. Such as:
 * {@link #createBasic(List)},
 * {@link #createFutured(List)},
 * {@link #createFutureOne(List)},
 * {@link #createListed(List)},
 * {@link #createResumable(List)},
 * {@link #createUnary(List)}
 * @apiNote The poller classes {@link Poller} and {@link SlimPoller}
 * simply correspond to {@link Mediator} and {@link SlimPoller}
 * The difference being, pollers can add and remove {@code observers}
 * <p>
 * Pollers do not extend mediator because inheritance would not work or intuitively.
 * @see SlimMediator
 */
public interface SlimPoller<TContext, TResponse, TObserver> {

    /**
     * Notifies this object's observer(s) of the specified {@code context} and returns their response.
     * The number of observer and notification order are determined by an implementation.
     *
     * @param context the information to be passed to an observer(s)
     * @return the result of notification if it's immediately available, or null if waiting is required
     * @throws NullPointerException if context is null and null is not permitted
     * @throws InterruptedException if notification involves waiting on an interruptible observer
     */
    List<TResponse> notify(TContext context) throws InterruptedException;

    /**
     * Adds {@code observers} who will be notified when {@link #notify(Object)} is called.
     *
     * @param observer the observer to add
     * @return true if added
     * @throws NullPointerException if observer is null and null is not permitted
     * @throws UnsupportedOperationException if adding is not supported by this poller
     */
    boolean addObserver(TObserver observer);

    /**
     * Removes the specified {@code observer} from this object's collection
     *
     * @param observer the observer to remove
     * @return true if removed
     * @throws UnsupportedOperationException if removing is not supported by this poller
     */
    boolean removeObserver(TObserver observer);

    /**
     * Returns a view of this objects observers
     * The default expectation is that the view is a copy or unmodifiable.
     *
     * @return a view of this objects observers
     * @implSpec this method is not implemented at default, because it's viewed as optional
     * @throws UnsupportedOperationException if the default behavior was not overridden or
     * viewing observers is not supported
     */
   default List<TObserver> viewObservers(){
       throw new UnsupportedOperationException("This method is not implemented yet");
   }

    /**
     * Removes all {@code observers} from this object's collection
     */
    void clearObservers();


//implementations

    abstract class Abstract<TContext, TReponse, TObserver> implements SlimPoller<TContext, TReponse, TObserver> {

        /**
         * returns the backing {@code collection} of Observers. It is expected that modifications
         * to the returned {@code collection} effect the this object's stored observers.
         */
        protected abstract Collection<TObserver> getObservers();

        @Override
        public boolean addObserver(TObserver tObserver) {
            return getObservers().add(tObserver);
        }

        @Override
        public boolean removeObserver(TObserver tObserver) {
            return getObservers().remove(tObserver);
        }

        @Override
        public void clearObservers() {
            getObservers().clear();
        }

        /**
         * Returns a list that is copy of this object's observers
         * {@inheritDoc}
         */
        @Override
        public List<TObserver> viewObservers() {
            return new ArrayList<>(getObservers());
        }

        /** this class is used to helps create instances that merely delegate */
        private static abstract class Delegate<T, R> implements SlimPoller<T, R, com.example.doruked.responder.Observer<T, R>> {

            private final SlimPoller<T, R, com.example.doruked.responder.Observer<T, R>> delegate;

            public Delegate(SlimPoller<T, R, com.example.doruked.responder.Observer<T, R>> delegate) {
                this.delegate = delegate;
            }

            @Override
            public List<R> notify(T t) throws InterruptedException {
                return delegate.notify(t);
            }

            @Override
            public boolean addObserver(com.example.doruked.responder.Observer<T, R> trObserver) {
                return delegate.addObserver(trObserver);
            }

            @Override
            public boolean removeObserver(com.example.doruked.responder.Observer<T, R> trObserver) {
                return delegate.removeObserver(trObserver);
            }

            @Override
            public void clearObservers() {
                delegate.clearObservers();
            }

            @Override
            public List<com.example.doruked.responder.Observer<T, R>> viewObservers() {
                return delegate.viewObservers();
            }
        }

    }

    /**
     * A basic instance of {@link SlimPoller}
     *
     * @param <T> the type of information notified by this poller
     * @param <R> the type returned after notification
     */
    class Basic<T, R> extends Abstract<T, R, com.example.doruked.responder.Observer<T, R>> {

        private final List<com.example.doruked.responder.Observer<T, R>> observers;

        public Basic(List<com.example.doruked.responder.Observer<T, R>> observers) {
            this.observers = observers;
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException {@inheritDoc}
         * @implSpec notifies each contained observer in their contained order
         */
        @Override
        public List<R> notify(T context) throws InterruptedException {
            List<R> result = new ArrayList<>();
            for (com.example.doruked.responder.Observer<T, R> observer : observers) {
                result.add(
                        observer.process(context)
                );
            }
            return result;
        }

        @Override
        protected Collection<com.example.doruked.responder.Observer<T, R>> getObservers() {
            return observers;
        }
    }

    /**
     * An instance of {@link Poller} that attempts to be resumable when notifying.
     * When notification is interrupted,
     *
     * @param <T> the type of information notified by this poller
     * @param <R> the type returned after notification
     */
    class Resumable<T, R> extends Abstract<T, R, com.example.doruked.responder.Observer<T, R>> {

        private final List<com.example.doruked.responder.Observer<T, R>> observers;
        private final Map<Integer, List<R>> interrupted = new HashMap<>();


        public Resumable(List<com.example.doruked.responder.Observer<T, R>> observers) {
            this.observers = observers;
        }

        @Override
        public List<R> notify(T context) throws InterruptedExceptionStamped {
            return helperNotify(context);
        }


        @Override
        protected Collection<com.example.doruked.responder.Observer<T, R>> getObservers() {
            return observers;
        }

        private List<R> helperNotify(T context) throws InterruptedExceptionStamped {
            BiConsumer<List<R>, Integer> storageAction = (list, index) -> interrupted.put(index,list);
            return notifyFromIndex(observers,context,0,storageAction);
        }

        /**
         * Retrieves the uncollected observer responses that occurred as a result of an interruption.
         * If none exist, an empty {@code list} is returned. After collection, the responses will
         * be removed from the specified {@code poller}
         *
         * @param poller the poller to get the missing responses from
         * @param ex the exception thrown by the computation that produced missing responses
         * @param <T> the type of information notified by this poller
         * @param <R> the type returned after notification
         * @return the uncollected observer responses or an empty list if none exist
         * @throws NullPointerException if (1) poller or ex is null
         * @throws IllegalArgumentException the message
         */
        public static <T, R> List<R> retrieveMissing(Resumable<T, R> poller, InterruptedExceptionStamped ex) {
            int id = ex.getId();
            if(!poller.interrupted.containsKey(id)) return Collections.emptyList();
            else return poller.interrupted.remove(id);
        }
    }


//extensions

    /**
     * Notification produces a single result of the same type as its operand
     *
     * @param <T> the type to notify of and retrieve
     */
    interface Unary<T> extends SlimPoller<T, T, com.example.doruked.responder.Observer<T, T>> {}

    /**
     * Each notification produces multiple objects the notified type
     *
     * @param <T> the type to notify and recieve a list of
     */
    interface Listed<T> extends SlimPoller<T, List<T>, com.example.doruked.responder.Observer<T, List<T> >> {}

    /**
     * Each notification generates a future {@code list}
     *
     * @param <T> the type to notify and recieve a future list of
     */
    interface Futured<T> extends SlimPoller<T, Future<List<T>>, com.example.doruked.responder.Observer<T,Future<List<T> >>> {}

    /**
     * Each notification produces a single future containing
     * the same type as it's operand
     *
     * @param <T> the type to notify and recieve a future of
     */
    interface FutureOne<T> extends SlimPoller<T, Future<T>, com.example.doruked.responder.Observer<T, Future<T>>> {}


//untilities


    /**
     * Attempts to notify some list of observers, starting from a specified {@code index}
     * "Notification" entails processing the given {@code context} with the given {@code observers}
     * <p>
     * This method attempts to provide the tools to resume notification if interrupted. When interrupted,
     * this method will throw  {@link InterruptedExceptionStamped}. This exception contains a message
     * with only of the index of the interrupted observer.
     * <p>
     * If an exception is thrown, this also means there are unretrieved responses. You may decide
     * what happens to them with the specified {@code onInterrupt}
     * <p>
     * Note: This method is not intended for general use and may suggest the use of some other
     * class/interface that provides finer control over which observers to notify.
     * Starting from {@code index 0} doesn't represent any concern. Using it in response to an
     * interruption is also acceptable
     *
     * @param observers   the observers to notify
     * @param context     the context to notify observers of
     * @param index       the index to start from
     * @param onInterrupt the action performed on observer responses. The list are the responses,
     *                    the integer is the index of the interrupted observer.
     * @return the responses from the observers
     * @throws InterruptedExceptionStamped   if the observer was interrupted
     * @throws UnsupportedOperationException if {@code onInterrupt} does not support the {@code add} operation
     * @throws NullPointerException          if context, observers or onInterrupt are null
     * @implSpec this method does not guarantee to work if observers are modified mid call.
     */
    static <T, R> List<R> notifyFromIndex(List<com.example.doruked.responder.Observer<T, R>> observers, T context, int index, BiConsumer<List<R>, Integer> onInterrupt) throws InterruptedExceptionStamped {
        Objects.requireNonNull(context);
        List<R> results = new ArrayList<>();
        int i = index;
        try {
            while (i < observers.size()) {
                com.example.doruked.responder.Observer<T, R> observer = observers.get(i);
                results.add(
                        observer.process(context)
                );
                i++;
            }
            return results;

        } catch (InterruptedException e) {
            onInterrupt.accept(results, i);
            throw new InterruptedExceptionStamped(String.valueOf(i));
        }
    }


//factories

    /**
     * Creates a new instance, using the the specified {@code delegate}
     * for behavior
     *
     * @param delegate the implementation to wrap
     * @param <E> the type of elements handled by the mediator
     * @return a new instance using the delegate
     */
    static <E> Unary<E> asUnary(SlimPoller<E, E, com.example.doruked.responder.Observer<E, E>> delegate) {

        class Impl<T> extends Abstract.Delegate<T, T> implements Unary<T> {
            private Impl(SlimPoller<T, T, com.example.doruked.responder.Observer<T, T>> delegate) {
                super(delegate);
            }
        }
        return new Impl<>(delegate);
    }

    /**
     * Creates a new instance, using the the specified {@code delegate}
     * for behavior
     *
     * @param delegate the implementation to wrap
     * @param <E> the type of elements handled by the mediator
     * @return a new instance using the delegate
     */
    static <E> Listed<E> asListed(SlimPoller<E, List<E>, com.example.doruked.responder.Observer<E, List<E>>> delegate) {

        class Impl<T> extends Abstract.Delegate<T, List<T>> implements Listed<T> {
            private Impl(SlimPoller<T, List<T>, com.example.doruked.responder.Observer<T, List<T>>> delegate) {
                super(delegate);
            }
        }
        return new Impl<>(delegate);
    }

    /**
     * Creates a new instance, using the the specified {@code delegate}
     * for behavior
     *
     * @param delegate the implementation to wrap
     * @param <E> the type of elements handled by the mediator
     * @return a new instance using the delegate
     */
    static <E> Futured<E> asFutured(SlimPoller<E, Future<List<E>>, com.example.doruked.responder.Observer<E, Future<List<E>>>> delegate) {

        class Impl<T> extends Abstract.Delegate<T, Future<List<T>>> implements Futured<T> {
            private Impl(SlimPoller<T, Future<List<T>>, com.example.doruked.responder.Observer<T, Future<List<T>>>> delegate) {
                super(delegate);
            }

            private SlimPoller checkInstance(){
               return delegate;
            }
        }
        return new Impl<>(delegate);
    }

    /**
     * Creates a new instance, using the the specified {@code delegate}
     * for behavior
     *
     * @param delegate the implementation to wrap
     * @param <E> the type of elements handled by the mediator
     * @return a new instance using the delegate
     */
    static <E> FutureOne<E> asFutureOne(SlimPoller<E, Future<E>, com.example.doruked.responder.Observer<E, Future<E>>> delegate) {

        class Impl<T> extends Abstract.Delegate<T, Future<T>> implements FutureOne<T> {
            private Impl(SlimPoller<T, Future<T>, com.example.doruked.responder.Observer<T, Future<T>>> delegate) {
                super(delegate);
            }
        }
        return new Impl<>(delegate);
    }

    /**
     * @param observers the observer to contain
     * @param <E> the type of element used by observer
     * @return a new instance
     */
    static <E, R> Basic<E, R> createBasic(List<com.example.doruked.responder.Observer<E, R>> observers) {
        return new Basic<>(observers);
    }

    /**
     * @param observers the observer to contain
     * @param <E> the type to notify observers of
     * @param <R> the type returned by observers after notification
     * @return a new instance
     */
    static <E,R> Resumable<E,R> createResumable(List<com.example.doruked.responder.Observer<E, R>> observers) {
        return new Resumable<>(observers);
    }

    /**
     * Creates new instance that delegates to {@link Basic}
     * for it's behavior
     *
     * @param observers the observer to contain
     * @param <E> the type of element used by observer
     * @return a new instance
     * @implSpec the type delegated to may change according to needs
     */
    static <E> Unary<E> createUnary(List<com.example.doruked.responder.Observer<E, E>> observers) {
        return asUnary(new Basic<>(observers));
    }

    /**
     * Creates new instance that delegates to {@link Basic}
     * for it's behavior
     *
     * @param observers the observer to contain
     * @param <E> the type of element used by observer
     * @return a new instance
     * @implSpec the type delegated to may change according to needs
     */
    static <E> Listed<E> createListed(List<com.example.doruked.responder.Observer<E, List<E>>> observers) {
        return asListed(new Basic<>(observers));
    }

    /**
     * Creates new instance that delegates to {@link Basic}
     * for it's behavior
     *
     * @param observers the observer to contain
     * @param <E> the type of element used by observer
     * @return a new instance
     * @implSpec the type delegated to may change according to needs
     */
    static <E> Futured<E> createFutured(List<com.example.doruked.responder.Observer<E, Future<List<E>>>> observers) {
        return asFutured(new Basic<>(observers));
    }

    /**
     * Creates new instance that delegates to {@link Basic}
     * for it's behavior
     *
     * @param observers the observer to contain
     * @param <E> the type of element used by observer
     * @return a new instance
     * @implSpec the type delegated to may change according to needs
     */
    static <E> FutureOne<E> createFutureOne(List<Observer<E, Future<E>>> observers) {
        return asFutureOne(new Basic<>(observers));
    }

}


