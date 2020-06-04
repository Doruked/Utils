package com.example.doruked;


/**
 *
 * @deprecated never really used and {@link UnsupportedOperationException}
 * can be used instead, with a message.
 */
@Deprecated
public class NewOperationException extends Exception {

    public NewOperationException() {
        super();
    }

    public NewOperationException(String message) {
        super(message);
    }
}
