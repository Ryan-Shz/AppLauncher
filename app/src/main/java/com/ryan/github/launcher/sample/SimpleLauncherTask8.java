package com.ryan.github.launcher.sample;

import android.util.Log;

import com.ryan.github.launcher.task.ILaunchTask;
import com.ryan.github.launcher.task.LaunchTask;

import java.util.ArrayList;
import java.util.List;

import static com.ryan.github.launcher.sample.Constants.TAG;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public class SimpleLauncherTask8 extends LaunchTask {

    @Override
    public List<Class<? extends ILaunchTask>> dependsOn() {
        List<Class<? extends ILaunchTask>> dependsOn = new ArrayList<>();
        dependsOn.add(SimpleLauncherTask10.class);
        return dependsOn;
    }

    @Override
    protected void call() {
        randomSleepTest();
        Log.v(TAG, "SimpleLauncherTask8 run on " + getThreadName() + ", depends on " + getDependsOnString());
    }

}
