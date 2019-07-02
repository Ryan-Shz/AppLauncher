package com.ryan.github.launcher.task;

import com.ryan.github.launcher.IAppLauncher;
import com.ryan.github.launcher.executor.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public abstract class LauncherTask implements ILauncherTask {

    private static final int STATE_CREATE = 0;
    private static final int STATE_WAITING = 1;
    private static final int STATE_RUNNING = 2;
    private static final int STATE_FINISHED = 3;

    private final Set<ILauncherTask> mChildTask;
    private CountDownLatch mWaitingLatch;
    private IAppLauncher mLauncher;
    private int mState = STATE_CREATE;

    public LauncherTask() {
        mChildTask = new HashSet<>();
        int dependsOnSize = dependsOn() == null ? 0 : dependsOn().size();
        if (dependsOnSize > 0) {
            mWaitingLatch = new CountDownLatch(dependsOnSize);
        }
    }

    @Override
    public final void run() {
        markState(STATE_WAITING);
        waitToSatisfy();
        markState(STATE_RUNNING);
        call();
        markState(STATE_FINISHED);
        notifyChildren();
        notifyLauncher();
    }

    protected abstract void call();

    @Override
    public List<Class<? extends ILauncherTask>> dependsOn() {
        return null;
    }

    @Override
    public Schedulers runOn() {
        return Schedulers.COMPUTATION;
    }

    private void waitToSatisfy() {
        if (mWaitingLatch != null) {
            try {
                mWaitingLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void satisfy() {
        if (mWaitingLatch != null) {
            mWaitingLatch.countDown();
        }
    }

    private void notifyChildren() {
        if (mChildTask != null && !mChildTask.isEmpty()) {
            for (ILauncherTask task : mChildTask) {
                task.satisfy();
            }
        }
    }

    private void notifyLauncher() {
        if (mLauncher != null) {
            if (mustFinishBeforeBreakPoint()) {
                mLauncher.satisfyBreakPoint();
            }
            mLauncher.onceTaskFinish();
        }
    }

    @Override
    public void addChildTask(ILauncherTask task) {
        mChildTask.add(task);
    }

    protected String getDependsOnString() {
        if (dependsOn() == null || dependsOn().isEmpty()) {
            return "";
        }
        StringBuilder output = new StringBuilder();
        for (Class clazz : dependsOn()) {
            output.append(clazz.getSimpleName());
            output.append(" | ");
        }
        return output.toString();
    }

    protected void randomSleepTest() {
        int random = new Random().nextInt(5) + 1;
        try {
            Thread.sleep(random * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void attachContext(IAppLauncher launcher) {
        mLauncher = launcher;
    }

    @Override
    public boolean isFinished() {
        return mState == STATE_FINISHED;
    }

    private void markState(int state) {
        mState = state;
    }

    @Override
    public boolean mustFinishBeforeBreakPoint() {
        return false;
    }
}
