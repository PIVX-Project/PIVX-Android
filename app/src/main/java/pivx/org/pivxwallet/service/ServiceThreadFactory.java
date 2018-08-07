package pivx.org.pivxwallet.service;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceThreadFactory implements ThreadFactory{

    private String threads_base_name;
    private static AtomicInteger num = new AtomicInteger(0);

    public ServiceThreadFactory(String threads_base_name) {
        this.threads_base_name = threads_base_name;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(threads_base_name+"_"+num.getAndIncrement());
        return thread;
    }
}
