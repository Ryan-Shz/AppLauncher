package com.ryan.github.launcher.task;

import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;
import android.util.Log;

import com.ryan.github.launcher.BuildConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public class TaskSortUtil {

    private static final String TAG = TaskSortUtil.class.getSimpleName();

    public static synchronized List<ILauncherTask> getSortResult(List<ILauncherTask> tasks) {
        List<Class<? extends ILauncherTask>> taskClassList = cloneClassList(tasks);
        Set<Integer> dependSet = new ArraySet<>();
        TaskGraph graph = new TaskGraph(tasks.size());
        for (int i = 0; i < tasks.size(); i++) {
            ILauncherTask task = tasks.get(i);
            List<Class<? extends ILauncherTask>> dependsTaskClasses = task.dependsOn();
            if (dependsTaskClasses == null || dependsTaskClasses.isEmpty()) {
                continue;
            }
            for (Class cls: dependsTaskClasses) {
                int dependIndex = taskClassList.indexOf(cls);
                if (dependIndex < 0) {
                    throw new IllegalStateException(task.getClass().getSimpleName()
                            + " depends on " + cls.getSimpleName()
                            + " can not be found in task list.");
                }
                tasks.get(dependIndex).addChildTask(task);
                dependSet.add(dependIndex);
                graph.addEdge(dependIndex, i);
            }
        }
        List<Integer> topologicalSortList = graph.topologicalSort();
        return buildSortedTasks(tasks, dependSet, topologicalSortList);
    }

    private static List<Class<? extends ILauncherTask>> cloneClassList(List<ILauncherTask> tasks) {
        List<Class<? extends ILauncherTask>> clsLaunchTasks = new ArrayList<>();
        for (ILauncherTask task : tasks) {
            clsLaunchTasks.add(task.getClass());
        }
        return clsLaunchTasks;
    }

    @NonNull
    private static List<ILauncherTask> buildSortedTasks(List<ILauncherTask> tasks,
                                                        Set<Integer> dependSet,
                                                        List<Integer> topologicalSortList) {
        int totalSize = tasks.size();
        int dependedSize = dependSet.size();
        List<ILauncherTask> sortedTask = new ArrayList<>(totalSize);
        List<ILauncherTask> tasksDepended = new ArrayList<>(dependedSize);
        List<ILauncherTask> tasksWithoutDepend = new ArrayList<>(totalSize - dependedSize);
        for (int index : topologicalSortList) {
            if (dependSet.contains(index)) {
                tasksDepended.add(tasks.get(index));
            } else {
                ILauncherTask task = tasks.get(index);
                tasksWithoutDepend.add(task);
            }
        }
        // 顺序：被别人依赖的————》需要提升自己优先级的————》需要被等待的————》没有依赖的
        sortedTask.addAll(tasksDepended);
        sortedTask.addAll(tasksWithoutDepend);
        debugInfo(sortedTask);
        return sortedTask;
    }

    private static void debugInfo(List<ILauncherTask> newTasksAll) {
        if (BuildConfig.DEBUG) {
            for (ILauncherTask task : newTasksAll) {
                Log.i(TAG, task.getClass().getSimpleName());
            }
        }
    }
}
