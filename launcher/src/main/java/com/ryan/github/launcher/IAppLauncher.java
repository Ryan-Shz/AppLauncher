package com.ryan.github.launcher;

/**
 * Created by Ryan
 * at 2019/7/2
 */
public interface IAppLauncher {

    void breakPoint(String type);

    void breakPoint(String type, int timeout);

    void satisfyBreakPoint(String type);

    void onceTaskFinish();

    void start();

    void shutdown();
}
