package com.ryan.github.launcher;

/**
 * Created by Ryan
 * at 2019/7/2
 */
public interface IAppLauncher {

    void breakPoint();

    void breakPoint(int timeout);

    void satisfyBreakPoint();

    void onceTaskFinish();

    void start();

    void shutdown();
}
