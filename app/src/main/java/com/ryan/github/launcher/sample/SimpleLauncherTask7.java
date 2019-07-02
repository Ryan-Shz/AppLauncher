package com.ryan.github.launcher.sample;

import android.util.Log;

import com.ryan.github.launcher.task.LauncherTask;

import static com.ryan.github.launcher.sample.Constants.TAG;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public class SimpleLauncherTask7 extends LauncherTask {

    @Override
    protected void call() {
        randomSleepTest();
        Log.v(TAG, "SimpleLauncherTask7 execute run...");
    }

}
