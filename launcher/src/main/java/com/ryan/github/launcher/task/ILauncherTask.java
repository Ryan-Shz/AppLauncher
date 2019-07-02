package com.ryan.github.launcher.task;

import com.ryan.github.launcher.AppLauncher;
import com.ryan.github.launcher.IAppLauncher;
import com.ryan.github.launcher.executor.Schedulers;

import java.util.List;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public interface ILauncherTask extends Runnable {

    List<Class<? extends ILauncherTask>> dependsOn();

    Schedulers runOn();

    void satisfy();

    void addChildTask(ILauncherTask task);

    boolean mustFinishBeforeBreakPoint();

    void attachContext(IAppLauncher launcher);

    boolean isFinished();
}
