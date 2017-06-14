package org.furszy.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Runnable} wrapper that preserves the name of the thread after the runnable is
 * complete (for {@link Runnable}s that change the name of the Thread they use.)
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class NamePreservingRunnable implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(NamePreservingRunnable.class);

    /** The runnable name */
    private final String newName;

    /** The runnable task */
    private final Runnable runnable;

    /**
     * Creates a new instance of NamePreservingRunnable.
     *
     * @param runnable The underlying runnable
     * @param newName The runnable's name
     */
    public NamePreservingRunnable(Runnable runnable, String newName) {
        this.runnable = runnable;
        this.newName = newName;
    }

    /**
     * Run the runnable after having renamed the current thread's name 
     * to the new name. When the runnable has completed, set back the 
     * current thread name back to its origin. 
     */
    public void run() {
        Thread currentThread = Thread.currentThread();
        String oldName = currentThread.getName();

        if (newName != null) {
            setName(currentThread, newName);
        }

        try {
            runnable.run();
        } finally {
            setName(currentThread, oldName);
        }
    }

    /**
     * Wraps {@link Thread#setName(String)} to catch a possible {@link Exception}s such as
     * {@link SecurityException} in sandbox environments, such as applets
     */
    private void setName(Thread thread, String name) {
        try {
            thread.setName(name);
        } catch (SecurityException se) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Failed to set the thread name.", se);
            }
        }
    }
}