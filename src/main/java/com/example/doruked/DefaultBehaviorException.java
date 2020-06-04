package com.example.doruked;

/**
 *
 * @deprecated never really used and {@link UnsupportedOperationException}
 * can be used instead, with a message.
 */
@Deprecated
public class DefaultBehaviorException  extends UnsupportedOperationException{

    public DefaultBehaviorException() {
        super("Method might be subject to change or removal");
    }

    public DefaultBehaviorException(String message) {
        super(message);
    }
}
