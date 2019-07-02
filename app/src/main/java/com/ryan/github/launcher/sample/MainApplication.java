package com.ryan.github.launcher.sample;

import android.app.Application;
import android.util.Log;

import com.ryan.github.launcher.AppLauncher;
import com.ryan.github.launcher.listener.IdleHandler;

import static com.ryan.github.launcher.sample.Constants.TAG;

/**
 * created by 2019/7/2 12:30 AM
 *
 * @author Ryan
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppLauncher launcher = new AppLauncher.Builder()
                .addTask(new SimpleLauncherTask7())
                .addTask(new SimpleLauncherTask2())
                .addTask(new SimpleLauncherTask3())
                .addTask(new SimpleLauncherTask1())
                .addTask(new SimpleLauncherTask4())
                .addTask(new SimpleLauncherTask6())
                .addTask(new SimpleLauncherTask5())
                .finishedHandler(new IdleHandler() {
                    @Override
                    public void onIdle() {
                        Log.v(TAG, "onIdle");
                    }
                }).start();
        launcher.breakPoint();
        Log.v(TAG, "onCreate Finished.");
    }
}
