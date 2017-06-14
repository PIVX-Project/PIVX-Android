package org.furszy.client.interfaces.write;

import java.util.Iterator;

/**
 * Created by mati on 12/05/17.
 */

public interface WriteRequestQueue {

    WriteRequest poll();

    void offer(WriteRequest writeRequest);

    boolean isEmpty();

    Iterator<WriteRequest> iterator();

}
