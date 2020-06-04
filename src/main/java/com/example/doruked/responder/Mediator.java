package com.example.doruked.responder;

import java.util.List;


/**
 * This interface is a {@link SlimMediator} that distinguishes the act of "notifying" from the act of getting a response.
 * The method {@link #notify(Object)} need not return an observer's response, and may signify another specified type.
 * However, calls to {@link #getResponse()} or {@link #getAllResponse()} are expected to be responses from observers.
 * <p>
 * UseCase: if there is a need for an {@link SlimMediator} with the added complexity
 * of storing responses. Or, each notification is not guaranteed to produce a response
 *
 * @param <TContext>  the type of information handled by this mediator
 * @param <TResponse> the type of response produced by observers
 * @param <TOptional> the type returned after notification
 * @implSpec It is still possible for  {@link #notify(Object)} to produce the observer's response
 * if declared to do so. Rather, this interface just allows that it can be difference.
 * @apiNote if the there is a desire to add or remove observers post construction
 * see ({@link SlimPoller} and {@link Poller})
 * @see Observer <TContext>
 */
public interface Mediator<TContext, TResponse,TOptional> extends SlimMediator<TContext, TOptional> {

    /**
     * Notifies this object's observer(s) of the specified {@code context}. The number of observer and notification
     * order are determined by an implementation.
     *
     * Once notified, this method returns {@link TOptional} which acts as an optional value defined by an implementation.
     *
     * @param context the information to be passed to an observer(s)
     * @return the result of notification if it's immediately available, or null if waiting is required
     * @throws NullPointerException if context is null and null is not permitted
     * @throws InterruptedException if notification involves waiting on an interruptible observer
     * @implSpec {@link TOptional} may be used to return the result of notifying or any other documented value.
     * In either case, observer's responses should be retrievable by {@link #getResponse()}
     */
    TOptional notify(TContext context) throws InterruptedException;

    /**
     * Returns a response from this object's observer.
     *
     * @return the response from this object's observer if available. Or else, another specified value
     * @throws InterruptedException if interrupted while retrieving the response
     * @implSpec This method does not specify it's behavior when dealing with multiple available responses.
     * When possible, it is expected that the retrieved response belongs to a previous
     * call of {@link #notify(Object)}. If notification has not occurred yet, or a response
     * is not eligible to be retrieved, the behavior of this method is not specified. It is
     * reasonably acceptable to have the method wait until an expected response is available.
     */
    TResponse getResponse() throws InterruptedException;

    /**
     * get all response or all expected responses
     * @throws InterruptedException if interrupted while retrieving the responses
     */
    List<TResponse> getAllResponse() throws InterruptedException;


    interface Single<TContext, TResponse> extends Mediator<TContext, TResponse, TResponse> { }

    interface Multi<TContext, TResponse> extends Mediator<TContext, TResponse, List<TResponse>> { }

}
