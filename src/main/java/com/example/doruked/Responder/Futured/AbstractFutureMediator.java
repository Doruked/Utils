package com.example.doruked.Responder.Futured;

import com.example.doruked.Responder.Mediator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This abstract class is designed to ease implementing a set of methods that retrieve values from {@link Future}s.
 * A logical structure is provide that relies on the use of helper methods. The basic logic for how
 * these methods will occur in {@link #helperAwait(boolean, long, TimeUnit)}.
 *
 * There is an assumption that uncollected responses after notification are stored in some way. In that light,
 * this interface reduces meaningful implementation to 3 points:
 * (1) how stored response are extracted and returned (see: {@link #poll()})
 * (2) how responses that failed to execute are recovered, if at all (see: {@link #recover(Future, ExecutionException)})
 * (3) how response are treated when they timeout (see: {@link #timedOut(Future, TimeoutException)})
 * (4) implementing {@link #notify(Object)}
 *
 * @param <TContext> the type of information handled by this mediator
 * @param <TResponse> the type of response produced by observers
 */
public abstract class AbstractFutureMediator<TContext, TResponse, TOptional> implements FutureMediator<TContext, TResponse,TOptional> {

    
//public operations

    @Override
    public Future<TResponse> getResponse() throws InterruptedException {
        return poll();
    }

    @Override
    public List<Future<TResponse>> getAllResponse() throws InterruptedException {
        return drain();
    }

    @Override
    public TResponse awaitMessage(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return await(true, true, timeout, unit);
    }

    @Override
    public TResponse awaitMessage() throws InterruptedException, ExecutionException {
        return helperAwait(true,true,-1, TimeUnit.MILLISECONDS);
    }

    @Override
    public List<TResponse> awaitAllMessages() throws InterruptedException, ExecutionException {
        return helperAwaitAll(responseSize(), true,true, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public List<TResponse> awaitAllMessages(int maximum, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
        return helperAwaitAll(maximum, true,true, timeout,unit);
    }


//abstract

    /**
     * Retrieves the{@code future}that this object will attempt to wait on.
     * @implSpec Essentially work like {@link Mediator#getResponse()} would.
     * should likely be the result of a previous {@link #notify(Object)} or {@code null} if non-existent
     */
    protected abstract Future<TResponse> poll() throws InterruptedException;

    /**
     * Retrieves all the futures that this object will attempt to wait on.
     * @implSpec Essentially work like {@link Mediator#getAllResponse()} would.
     */
    protected abstract List<Future<TResponse>> drain() throws InterruptedException;


    /**
     * This method decides how an implementation reacts to a {@link Future} that has thrown {@link ExecutionException}
     * and the {@code future} has been requested to be kept.
     *
     * @implNote the way this method is used currently follows this pattern (1) the exception is caught,
     * (2) this method is invoked, (3) the exception is rethrown.
     *
     * If the implementation of this method replaces the failed {@code future} back in it's original
     * container, it may be pulled again from methods that "awaitAll". If that would result in unwanted behavior,
     * then overriding {@link #helperAwaitAll(int, boolean, boolean, long, TimeUnit)} is necessary wherein
     * you specify the recovery behavior that occurs when awaiting multiple messages.
     *
     * @param failedToExecute the future that failed to execute
     */
    protected abstract void recover(Future<TResponse> failedToExecute, ExecutionException e);

    /**
     * returns the size of the container used to store the {@link Future<TResponse>}.
     *
     * @implSpec  If this object is not expected to store multiple {@code futures} and thus have a {@code 0} or constant
     * size, then the "awaitAll" methods that don't accept a explicit {@code maximum} may need to be overridden else they
     * will not behave as expected. They currently rely on the current size of this object.
     *
     * @return the current amount of {@link Future<TResponse>} retrievable by this class
     */
    protected abstract int responseSize();

    /**
     * Implement this method to define how responses that have thrown {@link TimeoutException} are handled.
     *
     * @param future the future that timed out
     * @param e the exception thrown
     * @param <T> the type of result contained by future
     * @throws TimeoutException if rethrowing the exception is wanted
     */
    protected abstract <T> void timedOut(Future<T> future, TimeoutException e) throws TimeoutException;

//helpers

    /**
     * override this to change how awaits occur for all methods (not guaranteed to be true if other methods are also overridden)
     *
     * The behavior of this method is more broad than what's used in this class. When called, {@code keepIfExecutionFails}
     * and {@code stopIfExecutionFails} will be {@code true}.
     * @param stopIfExecutionFails whether execution should throw {@link ExecutionException}
     */
    protected TResponse await(boolean keepIfExecutionFails, boolean stopIfExecutionFails, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Future<TResponse> future = poll();
        if (future == null) return null;

        if (keepIfExecutionFails) {
            try {
                if (timeout <= 0) return future.get();
                else return future.get(timeout, unit);
            } catch (ExecutionException e) {
                recover(future, e);
                if(stopIfExecutionFails) throw e;
                else return handle(future, e); //relevant when iterating and awaiting multiple futures. One might fail, and that means just move on
            } catch (TimeoutException e){
                timedOut(future, e);
                return null;
            }
        }
        else return future.get();
    }

    /**
     * Works like
     * {@link #await(boolean, boolean, long, TimeUnit)} but doesn't intend to throw {@link TimeoutException}
     * <p>
     * This is done for two reasons. (1) It's already handled by {@link #await(boolean, boolean, long, TimeUnit)}
     * or (2) The calling method does not intend to wait, so  timing out isn't appropriate
     */
    protected TResponse helperAwait(boolean keepIfExecutionFails, boolean stopIfExecutionFails, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
        try {
            return await(keepIfExecutionFails, stopIfExecutionFails, timeout, unit);
        } catch (TimeoutException e) {
            //do nothing,
            //already handles in {@code #timedou(Future, TimeoutException)}
        }
        return null;
    }

    /** override this to change how public methods named "awaitMessage" occur */
    protected TResponse helperAwait(boolean keepIfExecutionFails, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return await(keepIfExecutionFails, true, timeout, unit);
    }

    /**
     * Override this to change how public methods named "awaitAllMessage" occur
     *
     * This method attempts to retrieve responses, and makes attempts equal to the specified {@code maximum}
     * On each attempt, the method fully waits for the specified {@code time} and {@code unit}.
     * If an attempt times out, that {@code future} is handled according to {@link #timedOut(Future, TimeoutException)},
     * and the method moves on if possible.
     *
     * @param maximum the max objects to await for
     * @param keepIfExecutionFails whether the retrieved future is kept when it throws {@link ExecutionException}
     * @param stopOnExecutionFailure whether execution should throw {@link ExecutionException}
     * @param timeout the maximum time to wait for each response
     * @param unit the time unit of the timeout argument
     */
    protected List<TResponse> helperAwaitAll(int maximum, boolean keepIfExecutionFails, boolean stopOnExecutionFailure, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
        List<TResponse> results = new ArrayList<>();
        TResponse result;

        for (int i = 0; i < maximum; i++) {
            result = helperAwait(keepIfExecutionFails, stopOnExecutionFailure, timeout, unit);
            if (result != null)
                results.add(result);
        }
        return results;
    }

    /**
     * This method handles a caught {@code exception}. The idea is to allow implementors to
     * specify logging behavior etc.
     *
     * @implSpec This method should return {@code null} or document it's reasons for not doing so
     * in this method and the other methods of this classes where the value would be thrown.
     * The value returned is suppose to represent an error value given that execution has failed.
     *
     * @param e the exception to handle
     * @return null or a specified value for when execution fails
     */
    protected TResponse handle(Future<TResponse> failed, ExecutionException e) {
        throw new IllegalStateException("The path to this method is not supported by default");
    }


//inner class

    public static abstract class Directed<TContext, TResponse, TOptional> extends AbstractFutureMediator<TContext, TResponse, TOptional> implements DirectedFutureMediator<TContext, TResponse, TOptional> {

    //protected operations

        protected abstract TResponse handle(Future<TResponse> failed, ExecutionException e);

        /** override this to change how awaits occur for all methods (not guaranteed to be true if other methods are also overridden) */
        protected TResponse await(boolean keepIfExecutionFails, boolean stopIfExecutionFails, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
          return super.await(keepIfExecutionFails, stopIfExecutionFails, timeout, unit);
        }

        /**
         * Adds the specified{@code response} to the head of where response are collected, such that it would
         * be the next response to be collected by {@link #getAllResponse()}
         *
         * @param response the response to re-add
         */
        protected abstract void reAddFirst(Future<TResponse> response);

        /**
         * This method was overridden change how {@link Future}s that failed to execution are recovered in the context of
         * processing multiple {@code futures}.
         *
         * @implSpec the changes of this method aim to be somewhat consistent with {@link #recover(Future, ExecutionException)}.
         * When that method is invoked, {@code futures} that fail are re-added to the head of this objects collection of responses
         * In this method, {@code futures} that fail are stored are re-added to the head of tthis objects collection of responses
         * after being interrupted or completing. This prevents failed {@code futures} from being pulled
         * multiple times in a single "awaitAll".
         */
        @Override
        protected List<TResponse> helperAwaitAll(int maximum, boolean keepIfExecutionFails, boolean stopOnExecutionFailure, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
            List<TResponse> results = new ArrayList<>();
            List<Future<TResponse>> failed = new ArrayList<>();
            TResponse result;

            for (int i = 0; i < maximum; i++) {
                try {
                    Future<TResponse> future = poll();

                    if (stopOnExecutionFailure) {
                        result = future.get();
                    } else {
                        try {
                            result = future.get();
                        } catch (ExecutionException e) {
                            failed.add(future);
                            result = null;
                            handle(future, e);
                        }
                    }
                    if (result != null)
                        results.add(result);

                } catch (InterruptedException interrupt) {
                    failed.forEach(this::reAddFirst);
                    throw interrupt;
                }
            }
            return results;
        }

    //public operations

        @Override
        public TResponse awaitMessage(boolean keepIfExecutionFails, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return helperAwait(keepIfExecutionFails,timeout, unit);
        }

        @Override
        public TResponse awaitMessage(boolean keepIfExecutionFails) throws InterruptedException, ExecutionException {
            return helperAwait(keepIfExecutionFails, true,0, TimeUnit.MILLISECONDS);
        }

        @Override
        public List<TResponse> awaitAllMessages(boolean keepIfExecutionFails) throws InterruptedException, ExecutionException {
            return awaitAllMessages(keepIfExecutionFails, true);
        }

        @Override
        public List<TResponse> awaitAllMessages(boolean keepIfExecutionFails, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
            return helperAwaitAll(responseSize(), keepIfExecutionFails, true, timeout, unit);
        }

        @Override
        public List<TResponse> awaitAllMessages(boolean keepIfExecutionFails, boolean stopOnExecutionFailure, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
            return helperAwaitAll(responseSize(), keepIfExecutionFails, stopOnExecutionFailure, timeout, unit);
        }

        @Override
        public List<TResponse> awaitAllMessages(boolean keepIfExecutionFails, boolean stopOnExecutionFailure) throws InterruptedException, ExecutionException {
            return helperAwaitAll(responseSize(),keepIfExecutionFails, stopOnExecutionFailure,0, TimeUnit.MILLISECONDS);
        }

        @Override
        public List<TResponse> awaitAllMessages(int maximum, boolean keepIfExecutionFails, boolean stopOnExecutionFailure) throws InterruptedException, ExecutionException {
            return helperAwaitAll(maximum,keepIfExecutionFails, stopOnExecutionFailure,0, TimeUnit.MILLISECONDS);
        }
    }
}
