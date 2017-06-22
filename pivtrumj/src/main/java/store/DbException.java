package store;

/**
 * Created by furszy on 6/22/17.
 */

public class DbException extends Exception {

    public DbException() {
    }

    public DbException(String s) {
        super(s);
    }

    public DbException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
