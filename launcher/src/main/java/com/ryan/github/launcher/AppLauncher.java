package com.ryan.github.launcher;

import android.util.Log;

import com.ryan.github.launcher.executor.Executors;
import com.ryan.github.launcher.listener.IdleHandler;
import com.ryan.github.launcher.task.ILauncherTask;
import com.ryan.github.launcher.task.TaskSortUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public class AppLauncher implements IAppLauncher {

    private static final String TAG = AppLauncher.class.getSimpleName();
    private final AtomicInteger mFinishedCount;
    private List<ILauncherTask> mLauncherTasks;
    private IdleHandler mIdleHandler;
    private CountDownLatch mBreakPointLatch;

    private AppLauncher() {
        mFinishedCount = new AtomicInteger(0);
    }

    @Override
    public void start() {
        if (mLauncherTasks == null || mLauncherTasks.isEmpty()) {
            return;
        }
        mLauncherTasks = TaskSortUtil.getSortResult(mLauncherTasks);
        for (ILauncherTask task : mLauncherTasks) {
            task.attachContext(this);
            Executors.get(task.runOn()).execute(task);
        }
    }

    @Override
    public void satisfyBreakPoint() {
        if (mBreakPointLatch != null) {
            mBreakPointLatch.countDown();
        }
    }

    @Override
    public void onceTaskFinish() {
        if (mFinishedCount.incrementAndGet() == mLauncherTasks.size()) {
            Log.v(TAG, "all tasks have been finished.");
            handleOnFinished();
        }
    }

    private void handleOnFinished() {
        if (mIdleHandler != null) {
            mIdleHandler.onIdle();
        }
    }

    private int countOfNeedWaitTask() {
        int count = 0;
        for (ILauncherTask task : mLauncherTasks) {
            if (task.mustFinishBeforeBreakPoint()
                    && !task.isFinished()) {
                ++count;
            }
        }
        return count;
    }

    @Override
    public void breakPoint() {
        breakPoint(0);
    }

    @Override
    public void breakPoint(int timeout) {
        int count = countOfNeedWaitTask();
        if (count > 0) {
            mBreakPointLatch = new CountDownLatch(count);
            try {
                if (timeout > 0) {
                    mBreakPointLatch.await(timeout, TimeUnit.MILLISECONDS);
                } else {
                    mBreakPointLatch.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static final class Builder {

        private List<ILauncherTask> mLauncherTasks;
        private IdleHandler mIdleHandler;

        public Builder() {
            mLauncherTasks = new ArrayList<>();
        }

        public Builder addTask(ILauncherTask task) {
            mLauncherTasks.add(task);
            return this;
        }

        public Builder finishedHandler(IdleHandler handler) {
            mIdleHandler = handler;
            return this;
        }

        public AppLauncher create() {
            AppLauncher launcher = new AppLauncher();
            launcher.mLauncherTasks = mLauncherTasks;
            launcher.mIdleHandler = mIdleHandler;
            return launcher;
        }

        public AppLauncher start() {
            AppLauncher launcher = create();
            launcher.start();
            return launcher;
        }

    }

}
