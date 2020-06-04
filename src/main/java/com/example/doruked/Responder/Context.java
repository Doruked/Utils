package com.example.doruked.Responder;


/**
 * A class with the common and expected usage to package and convey information
 * This information is usually used to communicate information between different objects.
 *
 * @param <T> the type of information to store
 * @apiNote when used to communicate information between objects,
 * consider the mutability of the stored data. Mutable context may lead
 * to context that do not accurately represent the moment they were created
 * (assuming that is desired).
 * @deprecated  not sure of the use of this interface, and if it does it's fairly minimal.
 * This may be transitioned to a marker interface, or plainly deleted.
 */
@Deprecated
public interface Context<T> {


    T getContext();

}
