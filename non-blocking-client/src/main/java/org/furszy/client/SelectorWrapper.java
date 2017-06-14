package org.furszy.client;


import org.furszy.client.basic.IoSessionImp;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mati on 12/05/17.
 */

public class SelectorWrapper {

    private AtomicBoolean wakeupCalled = new AtomicBoolean(false);
    private SelectorProvider selectorProvider;
    private Selector selector;

    public SelectorWrapper(SelectorProvider selectorProvider) throws IOException {
        this.selectorProvider = selectorProvider;
        this.selector = selectorProvider.openSelector();
    }

    /**
     * Check for connected sockets, interrupt when at least a connection is
     * processed (connected or failed to connect). All the client socket
     * descriptors processed need to be returned by {@link #selectedHandles()}
     *
     * @param timeout The timeout for the select() method
     * @return The number of socket having received some data
     * @throws Exception any exception thrown by the underlying systems calls
     */
    int select(long timeout) throws Exception{
        return selector.select(timeout);
    }


    Iterator<IoLooper.ConnectionRequest> selectedHandles(){
        return new IoIterator(selector.selectedKeys());
    }

    public void close(SocketChannel socketChannel) {
        SelectionKey selectionKey = socketChannel.keyFor(selector);
        if (selectionKey!=null){
            selectionKey.cancel();
        }
    }

    protected boolean finishConnect(SocketChannel handle) throws Exception {
        if (handle.finishConnect()) {
            SelectionKey key = handle.keyFor(selector);

            if (key != null) {
                key.cancel();
            }

            return true;
        }

        return false;
    }

    public SelectionKey getSelectionKeyForChannel(SocketChannel channel){
        SelectionKey key = channel.keyFor(selector);

        if ((key == null) || (!key.isValid())) {
            return null;
        }

        return key;
    }

    public void registerConnect(SocketChannel socketChannel, IoLooper.ConnectionRequest req) throws ClosedChannelException {
        socketChannel.register(selector, SelectionKey.OP_CONNECT, req);
    }

    public SelectionKey registerRead(SocketChannel socketChannel,IoSessionImp session) throws ClosedChannelException {
        return socketChannel.register(selector, SelectionKey.OP_READ, session);
    }

    public Iterator<IoLooper.ConnectionRequest> allHandles() {
        return new IoIterator(selector.keys());
    }

    public void wakeup() {
        wakeupCalled.set(true);
        selector.wakeup();
    }


    public boolean isBrokenConnection() throws IOException {
        // A flag set to true if we find a broken session
        boolean brokenSession = false;

        synchronized (selector) {
            // Get the selector keys
            Set<SelectionKey> keys = selector.keys();

            // Loop on all the keys to see if one of them
            // has a closed channel
            for (SelectionKey key : keys) {
                SelectableChannel channel = key.channel();

                if ((((channel instanceof DatagramChannel) && !((DatagramChannel) channel).isConnected()))
                        || ((channel instanceof SocketChannel) && !((SocketChannel) channel).isConnected())) {
                    // The channel is not connected anymore. Cancel
                    // the associated key then.
                    key.cancel();

                    // Set the flag to true to avoid a selector switch
                    brokenSession = true;
                }
            }
        }

        return brokenSession;
    }

    public boolean getWakeUpAndSet(boolean value) {
        return wakeupCalled.getAndSet(value);
    }

    public void registerNewSelector() throws IOException {
        synchronized (selector) {
            Set<SelectionKey> keys = selector.keys();

            // Open a new selector
            Selector newSelector = null;

            if (selectorProvider == null) {
                newSelector = Selector.open();
            } else {
                newSelector = selectorProvider.openSelector();
            }

            // Loop on all the registered keys, and registerConnect them on the new selector
            for (SelectionKey key : keys) {
                SelectableChannel ch = key.channel();

                // Don't forget to attache the session, and back !
                IoSessionImp session = (IoSessionImp) key.attachment();
                SelectionKey newKey = ch.register(newSelector, key.interestOps(), session);
                session.setSelectionKey(newKey);
            }

            // Now we can close the old selector and switch it
            selector.close();
            selector = newSelector;
        }
    }


    public Iterator<IoSessionImp> selectedSessions() {
        return new IoIterator(selector.selectedKeys());
    }

    public boolean isReadable(IoSessionImp session) {
        SelectionKey key = session.getSelectionKey();

        return (key != null) && key.isValid() && key.isReadable();
    }


    public boolean isWritable(IoSessionImp session) {
        SelectionKey key = session.getSelectionKey();

        return (key != null) && key.isValid() && key.isWritable();
    }

    public void setInterestedInWrite(SelectionKey key, boolean isInterested)throws Exception {

        if ((key == null) || !key.isValid()) {
            return;
        }

        int newInterestOps = key.interestOps();

        if (isInterested) {
            newInterestOps |= SelectionKey.OP_WRITE;
        } else {
            newInterestOps &= ~SelectionKey.OP_WRITE;
        }

        key.interestOps(newInterestOps);
    }

    public boolean isSelectorEmpty() {
        return selector.keys().isEmpty();
    }

    public Iterator<IoSessionImp> allSessions() {
        return  new IoIterator(selector.keys());
    }


    /**
     * An encapsulating iterator around the {@link Selector#selectedKeys()} or
     * the {@link Selector#keys()} iterator;
     */
    protected static class IoIterator<E> implements Iterator<E> {
        private final Iterator<SelectionKey> iterator;

        /**
         * Create this iterator as a wrapper on top of the selectionKey Set.
         *
         * @param keys
         *            The set of selected sessions
         */
        private IoIterator(Set<SelectionKey> keys) {
            iterator = keys.iterator();
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        public E next() {
            SelectionKey key = iterator.next();
            E nioSession = (E) key.attachment();
            return nioSession;
        }

        /**
         * {@inheritDoc}
         */
        public void remove() {
            iterator.remove();
        }
    }

}
