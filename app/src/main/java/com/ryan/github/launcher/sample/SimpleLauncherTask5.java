package com.ryan.github.launcher.sample;

import android.util.Log;

import com.ryan.github.launcher.task.LaunchTask;
import com.ryan.github.launcher.task.ILaunchTask;

import java.util.ArrayList;
import java.util.List;

import static com.ryan.github.launcher.sample.Constants.TAG;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public class SimpleLauncherTask5 extends LaunchTask {

    @Override
    public List<Class<? extends ILaunchTask>> dependsOn() {
        List<Class<? extends ILaunchTask>> dependsOn = new ArrayList<>();
        dependsOn.add(SimpleLauncherTask7.class);
        dependsOn.add(SimpleLauncherTask1.class);
        dependsOn.add(SimpleLauncherTask2.class);
        return dependsOn;
    }

    @Override
    protected void call() {
        randomSleepTest();
        Log.v(TAG, "SimpleLauncherTask5 run on " + getThreadName() + ", depends on " + getDependsOnString());
    }

}
