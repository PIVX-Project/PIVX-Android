package org.furszy.client.exceptions;

/**
 * Created by mati on 12/05/17.
 */

public class ConnectionFailureException extends Exception {

    public ConnectionFailureException() {
    }

    public ConnectionFailureException(String s) {
        super(s);
    }

    public ConnectionFailureException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ConnectionFailureException(Throwable throwable) {
        super(throwable);
    }
}
