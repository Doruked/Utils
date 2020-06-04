package com.example.doruked;

/**
 *
 * @deprecated never really used and {@link UnsupportedOperationException}
 * can be used instead, with a message.
 */
@Deprecated
public class TemporaryDefaultBehaviorException extends Exception {

    public TemporaryDefaultBehaviorException(String message) {
        super(message);
    }

    public TemporaryDefaultBehaviorException() {
        super("Method might change or be removed");
    }
}
