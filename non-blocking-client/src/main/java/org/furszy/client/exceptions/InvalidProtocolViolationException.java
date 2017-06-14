package org.furszy.client.exceptions;

/**
 * Created by mati on 11/05/17.
 */

public class InvalidProtocolViolationException extends Exception {

    public InvalidProtocolViolationException() {
    }

    public InvalidProtocolViolationException(String s) {
        super(s);
    }

    public InvalidProtocolViolationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
