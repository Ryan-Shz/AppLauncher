package com.ryan.github.launcher.task;

import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;
import android.util.Log;

import com.ryan.github.launcher.BuildConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * sort all tasks.
 *
 * Created by Ryan
 * at 2019/7/1
 */
public class TaskSortUtil {

    private static final String TAG = TaskSortUtil.class.getSimpleName();

    public static synchronized List<ILaunchTask> getSortResult(List<ILaunchTask> normalTasks,
                                                               List<ILaunchTask> headTasks,
                                                               List<ILaunchTask> tailTasks) {
        int headTasksSize = headTasks.size();
        int normalTasksSize = normalTasks.size();
        int mergeTasksSize = normalTasks.size() + headTasksSize + tailTasks.size();
        List<ILaunchTask> mergeTasksList = new ArrayList<>(mergeTasksSize);
        mergeTasksList.addAll(headTasks);
        mergeTasksList.addAll(normalTasks);
        mergeTasksList.addAll(tailTasks);

        List<Class<? extends ILaunchTask>> mergeClassList = cloneClassList(mergeTasksList);
        List<Class<? extends ILaunchTask>> headClassList = cloneClassList(headTasks);
        List<Class<? extends ILaunchTask>> midClassList = cloneClassList(normalTasks);

        Set<Integer> dependSet = new ArraySet<>();
        TaskGraph graph = new TaskGraph(mergeTasksSize);

        int tailTaskStartIndex = headTasksSize + normalTasksSize;

        for (int i = 0; i < mergeTasksList.size(); i++) {
            ILaunchTask task = mergeTasksList.get(i);
            List<Class<? extends ILaunchTask>> dependsTaskClasses = task.dependsOn();
            if (dependsTaskClasses == null) {
                dependsTaskClasses = new ArrayList<>();
            }

            if (i >= headTasksSize && i < tailTaskStartIndex) {
                // process normal tasks
                dependsTaskClasses.addAll(headClassList);
            } else if (i >= headTasksSize + normalTasksSize) {
                // process tail tasks
                dependsTaskClasses.addAll(midClassList);
            }
            if (dependsTaskClasses.isEmpty()) {
                continue;
            }
            task.updateDependsCount(dependsTaskClasses.size());
            for (Class cls : dependsTaskClasses) {
                int dependIndex = mergeClassList.indexOf(cls);
                if (dependIndex < 0) {
                    throw new IllegalStateException(task.getClass().getSimpleName()
                            + " depends on " + cls.getSimpleName()
                            + " can not be found in task list.");
                }
                mergeTasksList.get(dependIndex).addChildTask(task);
                dependSet.add(dependIndex);
                graph.addEdge(dependIndex, i);
            }
        }
        List<Integer> topologicalSortList = graph.topologicalSort();
        return buildSortedTasks(mergeTasksList, dependSet, topologicalSortList);
    }

    private static List<Class<? extends ILaunchTask>> cloneClassList(List<ILaunchTask> tasks) {
        List<Class<? extends ILaunchTask>> clsLaunchTasks = new ArrayList<>();
        for (ILaunchTask task : tasks) {
            clsLaunchTasks.add(task.getClass());
        }
        return clsLaunchTasks;
    }

    @NonNull
    private static List<ILaunchTask> buildSortedTasks(List<ILaunchTask> tasks,
                                                      Set<Integer> dependSet,
                                                      List<Integer> topologicalSortList) {
        int totalSize = tasks.size();
        int dependedSize = dependSet.size();
        List<ILaunchTask> sortedTask = new ArrayList<>(totalSize);
        List<ILaunchTask> tasksDepended = new ArrayList<>(dependedSize);
        List<ILaunchTask> tasksWithoutDepend = new ArrayList<>(totalSize - dependedSize);
        for (int index : topologicalSortList) {
            if (dependSet.contains(index)) {
                tasksDepended.add(tasks.get(index));
            } else {
                ILaunchTask task = tasks.get(index);
                tasksWithoutDepend.add(task);
            }
        }
        // Order: head ---> is dependent on others --> no dependencies ---> tail
        sortedTask.addAll(tasksDepended);
        sortedTask.addAll(tasksWithoutDepend);
        debugInfo(sortedTask);
        return sortedTask;
    }

    private static void debugInfo(List<ILaunchTask> newTasksAll) {
        if (BuildConfig.DEBUG) {
            for (ILaunchTask task : newTasksAll) {
                Log.i(TAG, task.getClass().getSimpleName());
            }
        }
    }
}
