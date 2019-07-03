package com.ryan.github.launcher.executor;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public interface TaskExecutor {

    void execute(Runnable runnable);

    void shutdown();

}
