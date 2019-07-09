package com.ryan.github.launcher.sample;

import android.util.Log;

import com.ryan.github.launcher.task.LaunchTask;
import com.ryan.github.launcher.task.ILaunchTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ryan.github.launcher.sample.Constants.TAG;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public class SimpleLauncherTask6 extends LaunchTask {

    @Override
    public List<Class<? extends ILaunchTask>> dependsOn() {
        List<Class<? extends ILaunchTask>> dependsOn = new ArrayList<>();
        dependsOn.add(SimpleLauncherTask7.class);
        dependsOn.add(SimpleLauncherTask1.class);
        dependsOn.add(SimpleLauncherTask4.class);
        return dependsOn;
    }

    @Override
    protected void call() {
        randomSleepTest();
        Log.v(TAG, "SimpleLauncherTask6 run on " + getThreadName() + ", depends on " + getDependsOnString());
    }

    @Override
    public List<String> finishBeforeBreakPoints() {
        List<String> breakPoints = new ArrayList<>(1);
        breakPoints.add(BreakPoints.TYPE_APPLICATION_CREATE);
        return breakPoints;
    }
}
