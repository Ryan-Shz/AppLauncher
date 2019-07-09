package com.ryan.github.launcher.task;

import com.ryan.github.launcher.IAppLauncher;
import com.ryan.github.launcher.executor.Schedulers;

import java.util.List;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public interface ILaunchTask extends Runnable {

    List<Class<? extends ILaunchTask>> dependsOn();

    Schedulers runOn();

    void satisfy();

    void addChildTask(ILaunchTask task);

    List<String> finishBeforeBreakPoints();

    void attachContext(IAppLauncher launcher);

    boolean isFinished();

    void updateDependsCount(int count);
}
