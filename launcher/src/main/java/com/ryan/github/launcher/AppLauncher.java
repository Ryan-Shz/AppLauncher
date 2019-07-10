package com.ryan.github.launcher;

import android.os.Looper;
import android.os.MessageQueue;

import com.ryan.github.launcher.executor.Executors;
import com.ryan.github.launcher.executor.TaskExecutor;
import com.ryan.github.launcher.listener.IdleHandler;
import com.ryan.github.launcher.task.ILaunchTask;
import com.ryan.github.launcher.task.TaskSortUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public class AppLauncher implements IAppLauncher {

    private static final int STATE_PREPARE = 1;
    private static final int STATE_RUNNING = 2;
    private static final int STATE_FINISHED = 3;
    private static final int STATE_SHUTDOWN = 4;
    private final AtomicInteger mFinishedCount;
    private List<ILaunchTask> mLauncherTasks;
    private IdleHandler mIdleHandler;
    private Map<String, CountDownLatch> mBreakPointLatchMap;
    private List<ILaunchTask> mHeadTasks;
    private List<ILaunchTask> mTailTasks;
    private Queue<ILaunchTask> mDelayTasks;
    private Set<TaskExecutor> mExecutors;
    private boolean mShutDownAfterFinish;
    private MessageQueue mMainQueue;
    private volatile int mState = STATE_PREPARE;
    private volatile int mTaskCount;

    private AppLauncher() {
        mFinishedCount = new AtomicInteger(0);
        mExecutors = new HashSet<>();
    }

    @Override
    public void start() {
        checkThread();
        if (mState == STATE_SHUTDOWN) {
            throw new IllegalStateException("the Launcher has been shut down.");
        }
        if (mLauncherTasks == null || mLauncherTasks.isEmpty()) {
            return;
        }
        if (mState == STATE_RUNNING) {
            return;
        }
        if (mMainQueue == null) {
            mMainQueue = Looper.myQueue();
        }
        markState(STATE_RUNNING);
        mLauncherTasks = TaskSortUtil.getSortResult(mLauncherTasks, mHeadTasks, mTailTasks);
        mTaskCount = mLauncherTasks.size();
        executeTasks(mLauncherTasks);
    }

    @Override
    public void shutdown() {
        if (mState == STATE_SHUTDOWN) {
            return;
        }
        switch (mState) {
            case STATE_RUNNING:
                mShutDownAfterFinish = true;
                break;
            case STATE_FINISHED:
                shutdownAllExecutors();
            default:
                markState(STATE_SHUTDOWN);
        }
    }

    private synchronized void shutdownAllExecutors() {
        if (mExecutors.isEmpty()) {
            return;
        }
        for (TaskExecutor executor : mExecutors) {
            executor.shutdown();
        }
        mExecutors.clear();
        mExecutors = null;
    }

    @Override
    public void satisfyBreakPoint(String type) {
        CountDownLatch latch = getBreakPointLatch(type);
        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public void onceTaskFinish() {
        if (mFinishedCount.incrementAndGet() == mTaskCount) {
            markState(STATE_FINISHED);
            handleOnFinished();
            handDelayTasks();
        }
    }

    private void handleOnFinished() {
        if (mIdleHandler != null) {
            mIdleHandler.onIdle();
        }
        if (mShutDownAfterFinish) {
            shutdown();
        }
    }

    private void handDelayTasks() {
        if (mMainQueue == null || mState == STATE_PREPARE) {
            throw new IllegalStateException("The launcher has not started yet.");
        }
        if (mState == STATE_SHUTDOWN){
            throw new IllegalStateException("The launcher has been shut down.");
        }
        mMainQueue.addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                ILaunchTask task = mDelayTasks.poll();
                executeTasks(task);
                return !mDelayTasks.isEmpty();
            }
        });
    }

    private int countOfNeedWaitTask(String type) {
        int count = 0;
        for (ILaunchTask task : mLauncherTasks) {
            if (!task.isFinished()) {
                List<String> breakPoints = task.finishBeforeBreakPoints();
                if (breakPoints != null && breakPoints.contains(type)) {
                    ++count;
                }
            }
        }
        return count;
    }

    @Override
    public void breakPoint(String type) {
        breakPoint(type, 0);
    }

    @Override
    public void breakPoint(String type, int timeout) {
        int count = countOfNeedWaitTask(type);
        if (count > 0) {
            CountDownLatch breakPointLatch = obtainBreakPointLatch(type, count);
            try {
                if (timeout > 0) {
                    breakPointLatch.await(timeout, TimeUnit.MILLISECONDS);
                } else {
                    breakPointLatch.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized CountDownLatch obtainBreakPointLatch(String type, int count) {
        if (mBreakPointLatchMap == null) {
            mBreakPointLatchMap = new ConcurrentHashMap<>();
        }
        CountDownLatch latch = mBreakPointLatchMap.get(type);
        if (latch == null) {
            latch = new CountDownLatch(count);
            mBreakPointLatchMap.put(type, latch);
        }
        return latch;
    }

    private CountDownLatch getBreakPointLatch(String type) {
        if (mBreakPointLatchMap == null || mBreakPointLatchMap.isEmpty()) {
            return null;
        }
        return mBreakPointLatchMap.get(type);
    }

    public static final class Builder {

        private List<ILaunchTask> mLauncherTasks;
        private IdleHandler mIdleHandler;
        private List<ILaunchTask> mHeadTasks;
        private List<ILaunchTask> mTailTasks;
        private Queue<ILaunchTask> mDelayTasks;

        public Builder() {
            mLauncherTasks = new ArrayList<>();
            mHeadTasks = new ArrayList<>();
            mTailTasks = new ArrayList<>();
            mDelayTasks = new LinkedList<>();
        }

        public Builder addTask(ILaunchTask task) {
            mLauncherTasks.add(task);
            return this;
        }

        public Builder idleHandler(IdleHandler handler) {
            mIdleHandler = handler;
            return this;
        }

        public Builder addHeadTask(ILaunchTask task) {
            mHeadTasks.add(0, task);
            return this;
        }

        public Builder addTailTask(ILaunchTask task) {
            mTailTasks.add(task);
            return this;
        }

        public Builder addDelayTask(ILaunchTask task) {
            mDelayTasks.offer(task);
            return this;
        }

        public AppLauncher create() {
            AppLauncher launcher = new AppLauncher();
            launcher.mLauncherTasks = mLauncherTasks;
            launcher.mIdleHandler = mIdleHandler;
            launcher.mHeadTasks = mHeadTasks;
            launcher.mTailTasks = mTailTasks;
            launcher.mDelayTasks = mDelayTasks;
            return launcher;
        }

        public AppLauncher start() {
            AppLauncher launcher = create();
            launcher.start();
            return launcher;
        }

    }

    private void markState(int state) {
        mState = state;
    }

    private void executeTasks(List<ILaunchTask> tasks) {
        for (ILaunchTask task : tasks) {
            executeTasks(task);
        }
    }

    private void executeTasks(ILaunchTask task) {
        task.attachContext(this);
        TaskExecutor executor = Executors.get(task.runOn());
        mExecutors.add(executor);
        executor.execute(task);
    }

    private void checkThread() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("AppLauncher.start() must be executed on the main thread.");
        }
    }
}
