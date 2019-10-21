# AppLauncher

AppLauncher是一个轻量的Android App的任务启动器。

它是一个优秀的异步初始化解决方案，用来方便、快速的帮助APP处理异步初始化来达到应用启动的最佳性能。

## 原理

AppLauncher把初始化任务划分为一个个细小的LaunchTask，task之间可以互相依赖。

启动器在启动时，会根据所有任务的依赖关系构建一个有向无环图并生成拓扑序列，根据该序列使用多线程执行。

## Sample

### 创建一个Task

```
public class SimpleLauncherTask extends LauncherTask {

    @Override
    protected void call() {
        // 执行具体的初始化任务
        randomSleepTest();
        Log.v(TAG, "SimpleLauncherTask6 execute run...depends on " + getDependsOnString());
    }
}
```

### 构建Launcher

```
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new AppLauncher.Builder()
                .addTask(new SimpleLauncherTask7())
                .addTask(new SimpleLauncherTask2())
                .addTask(new SimpleLauncherTask3())
                .addTask(new SimpleLauncherTask1())
                .addTask(new SimpleLauncherTask4())
                .addTask(new SimpleLauncherTask6())
                .addTask(new SimpleLauncherTask5())
                .start();
    }
}
```

好了，这样就可以完成启动初始化了，是不是很简单呢？

## 其他用法

### 设置Task依赖

重写LaunchTask的dependsOn方法，返回依赖的任务类型列表：

```
public class SimpleLauncherTask extends LauncherTask {

    @Override
    public List<Class<? extends ILauncherTask>> dependsOn() {
    	// 返回依赖的Task列表，依赖后需要等待依赖的Task执行完成后才能继续执行
        List<Class<? extends ILauncherTask>> dependsOn = new ArrayList<>();
        dependsOn.add(SimpleLauncherTask7.class);
        dependsOn.add(SimpleLauncherTask1.class);
        dependsOn.add(SimpleLauncherTask4.class);
        return dependsOn;
    }
}
```

### 设置Task执行线程

AppLauncher中添加了3种线程模式：

1. Schedulars.MAIN  主线程执行
2. Schedulars.IO        IO线程执行
3. Schedulars.COMPUTATION   计算线程执行

通过重写launchTask的runOn方法，返回当前任务的线程模式：

```
public class SimpleLauncherTask1 extends LauncherTask {

    @Override
    protected void call() {
        randomSleepTest();
        Log.v(TAG, "SimpleLauncherTask1 run on " + getThreadName());
    }

    @Override
    public Schedulers runOn() {
        // SimpleLauncherTask1将在IO线程执行
        return Schedulers.IO;
    }
}
```

### 添加Head/Tail Task

head task将在所有task前执行：

```
new AppLauncher.Builder()
            .addTask(new SimpleLauncherTask1())
            .addTask(new SimpleLauncherTask2())
            .addTask(new SimpleLauncherTask3())
            // head task SimpleLauncherTask4将会第一个执行
            // SimpleLauncherTask4执行完成后才能继续执行其他task（1/2/3）
            .addHeadTask(new SimpleLauncherTask4())
            .start();
```

tail task将在所有task执行完成后执行：

```
new AppLauncher.Builder()
            .addTask(new SimpleLauncherTask1())
            .addTask(new SimpleLauncherTask2())
            .addTask(new SimpleLauncherTask3())
            // tail task SimpleLauncherTask4将会最后一个执行
            // task(1/2/3)执行完成后才能执行task4
            .addTailTask(new SimpleLauncherTask4())
            .start();
```

### 添加DelayTask

有时候，我们会有一些优先级不是那么高的Task，我们想让它在启动后延迟执行，并且对当前应用的影响降到最低。

为了解决这个问题，我们引入了DelayTask，DelayTask会等待所有Task执行完成并且在主线程空闲时执行。

```
new AppLauncher.Builder()
            .addTask(new SimpleLauncherTask1())
            .addDelayTask(new SimpleLaunchrTask2())
            .start();
```

### 设置断点

在介绍断点功能之前，我们思考这样一个令人苦恼的问题：“明明一个任务可以异步初始化，但是又要经常担心具体使用它的时候是否已经初始化完成了...为了避免出错，于是我们不得不把它放到主线程中同步初始化”。

就是为了解决这个问题，启动器里引入了断点功能。

启动器可以调用breakPoint方法来执行一个断点:

```
launcher.breakPoint(String type);
launcher.breakPoint(String type, int timeout);
```

断点的意思是线程将在这里等待，直到所有设置了这个断点类型的task执行完成.

Task可以通过重写finishBeforeBreakPoints方法来设置自己的断点类型列表，表示启动器中如果这些类型的断点在执行等待时，则必须等到它执行完成后才能继续向下执行。

```
public class SimpleLauncherTask extends LaunchTask {

    @Override
    public List<String> finishBeforeBreakPoints() {
        List<String> breakPoints = new ArrayList<>(1);
        breakPoints.add(BreakPoints.TYPE_APPLICATION_CREATE);
        return breakPoints;
    }
}
```

这有个好处，例如，你有某个任务必须需要在Application的onCreate方法中执行完成，那么可以这样：

1. 在Application onCreate方法的末尾调用launcher.breakPoint(String type)

```
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppLauncher launcher = new AppLauncher.Builder()
                .addTask(new SimpleLauncherTask1())
                .addTask(new SimpleLauncherTask2())
                .addTask(new SimpleLauncherTask3())        
                .start();
                
        // ...do something
        
        // 在这里等待，直到所有设置了BreakPoints.TYPE_APPLICATION_CREATE类型的task执行完成
        launcher.breakPoint(BreakPoints.TYPE_APPLICATION_CREATE);
        
        // 如果你担心等待太久的话，可以执行launcher.breakPoint(String type, int timeout)
        // 传入等待超时时间，等待指定的时间后会继续向下执行
        // eg: launcher.breakPoint(BreakPoints.TYPE_APPLICATION_CREATE， 1000);
        
        Log.v(TAG, "application onCreate Finished.");
    }
}
```

2. 重写LaunchTask的finishBeforeBreakPoints方法，返回断点类型列表：

```
public class SimpleLauncheTask extends LauncherTask {

    @Override
    public List<String> finishBeforeBreakPoints() {
        List<String> breakPoints = new ArrayList<>(1);
        breakPoints.add(BreakPoints.TYPE_APPLICATION_CREATE);
        return breakPoints;
    }
}
```

在SimpleLaunchTask任务执行完成之前，application的onCreate将会一直等待。

是不是很简单呢？

###  IdleHandler

如果你想在所有任务执行完成后做一些事情，那么你可以设置一个idleHander，例如这样：

```
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
                // 设置一个idleHander
                .idleHandler(new IdleHandler() {
                    @Override
                    public void onIdle() {
                        Log.v(TAG, "all tasks have been finished.");
                    }
                }).start();
    }
}
```

### 销毁启动器

如果你想在所有任务执行完成后销毁启动器并释放资源，那么可以这样：

```
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppLauncher launcher = new AppLauncher.Builder()
                .addTask(new SimpleLauncherTask1())
                .addTask(new SimpleLauncherTask2())
                .addTask(new SimpleLauncherTask3())
                .start();
        // task(1/2/3)都执行完成后才会销毁启动器
        launcher.shutdown();
    }
}
```

如果Launcher已经调用start后调用shutdown方法，Launcher也会执行完所有的task后再销毁启动器。
