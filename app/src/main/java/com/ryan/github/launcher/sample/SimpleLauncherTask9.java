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
public class SimpleLauncherTask9 extends LaunchTask {

    @Override
    public List<Class<? extends ILaunchTask>> dependsOn() {
        List<Class<? extends ILaunchTask>> depends = new ArrayList<>();
        depends.add(SimpleLauncherTask12.class);
        return depends;
    }

    @Override
    protected void call() {
        randomSleepTest();
        Log.v(TAG, "SimpleLauncherTask9 run on " + getThreadName() + ", depends on " + getDependsOnString());
    }

}
