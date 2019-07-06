package com.ryan.github.launcher.executor;

import android.os.Process;
import android.support.annotation.NonNull;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public class IOExecutor implements TaskExecutor {

    private static final String IO_THREAD_PREFIX = "launcher-io-";
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private AtomicInteger mName = new AtomicInteger(0);
    private ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, CORE_POOL_SIZE, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, IO_THREAD_PREFIX + mName.incrementAndGet()){
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_LESS_FAVORABLE);
                    super.run();
                }
            };
        }
    });

    public static IOExecutor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final IOExecutor INSTANCE = new IOExecutor();
    }

    private IOExecutor() {
        mExecutor.allowCoreThreadTimeOut(true);
    }

    @Override
    public void execute(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    @Override
    public void shutdown() {
        mExecutor.shutdown();
    }
}
