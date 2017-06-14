package org.furszy.client.basic;

import org.furszy.client.interfaces.write.WriteRequest;
import org.furszy.client.interfaces.write.WriteRequestQueue;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by mati on 14/05/17.
 */

public class WriteRequestQueueImp implements WriteRequestQueue {

    ConcurrentLinkedQueue<WriteRequest> queue = new ConcurrentLinkedQueue<>();

    public WriteRequestQueueImp() {
    }

    @Override
    public WriteRequest poll() {
        return queue.poll();
    }

    @Override
    public void offer(WriteRequest writeRequest) {
        queue.offer(writeRequest);
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public Iterator<WriteRequest> iterator() {
        return queue.iterator();
    }
}
