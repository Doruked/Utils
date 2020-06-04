package com.example.doruked.Responder;

import java.util.List;

/**
 * This interface is a {@link SlimPoller} that distinguishes the act of "notifying" from the act of getting a response.
 * The method {@link #notify(Object)} need not return an observer's response, and may signify another specified type.
 * However, calls to {@link #getResponse()} or {@link #getAllResponse()} are expected to be responses from observers.
 * <p>
 * UseCase: if there is a need for an {@link SlimPoller} with the added complexity
 * of storing responses. Or, each notification is not guaranteed to produce a response
 *
 * @apiNote The poller classes {@link Poller} and {@link SlimPoller}
 * simply correspond to {@link Mediator} and {@link SlimMediator}
 * The difference being, pollers can add and remove {@code observers}
 * <p>
 * Pollers do not extend mediator because inheritance would not work or intuitively.
 * @see Mediator
 */
public interface Poller<TContext, TResponse, TOptional, TObserver> extends SlimPoller<TContext, TOptional, TObserver> {



   List<TResponse> getResponse();

   List<List<TResponse>> getAllResponse();


}
