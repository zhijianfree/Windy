package com.zj.common.monitor.pool;

import com.alibaba.ttl.threadpool.TtlExecutors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.util.Assert;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureTask;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Getter
@Slf4j
public class WindyThreadPool extends ExecutorConfigurationSupport implements
    AsyncListenableTaskExecutor {

  public static final int MAX_SHOW_SPEND_TIME = 20000;
  private Integer corePoolSize = 10;
  private Integer maxPoolSize = 20;
  private Long timeout = 60L;
  private Integer queueSize = 100;
  private boolean allowCoreThreadTimeOut;
  private final Map<String, Long> timeCache = new ConcurrentHashMap(128);
  private ThreadPoolExecutor executor;

  public ThreadPoolExecutor getThreadPoolExecutor() throws IllegalStateException {
    Assert.state(Objects.nonNull(executor), "ThreadPoolTaskExecutor not initialized");
    return executor;
  }

  public void execute(Runnable task) {
    Executor executor = this.getThreadPoolExecutor();

    try {
      TtlExecutors.getTtlExecutor(executor).execute(WrapperRunnable.wrapper(task));
    } catch (RejectedExecutionException rejectedExecutionException) {
      throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task,
          rejectedExecutionException);
    }
  }

  public void execute(Runnable task, long startTimeout) {
    this.execute(task);
  }

  public Future<?> submit(Runnable task) {
    ExecutorService executor = this.getThreadPoolExecutor();
    try {
      return TtlExecutors.getTtlExecutorService(executor).submit(WrapperRunnable.wrapper(task));
    } catch (RejectedExecutionException rejectedExecutionException) {
      throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task,
          rejectedExecutionException);
    }
  }

  public <T> Future<T> submit(Callable<T> task) {
    ExecutorService executor = this.getThreadPoolExecutor();
    try {
      return TtlExecutors.getTtlExecutorService(executor).submit(WrapperRunnable.wrapperCall(task));
    } catch (RejectedExecutionException rejectedExecutionException) {
      throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task,
          rejectedExecutionException);
    }
  }

  public ListenableFuture<?> submitListenable(Runnable task) {
    ExecutorService executor = this.getThreadPoolExecutor();

    try {
      ListenableFutureTask<Object> future = new ListenableFutureTask(task, (Object) null);
      executor.execute(future);
      return future;
    } catch (RejectedExecutionException rejectedExecutionException) {
      throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task,
          rejectedExecutionException);
    }
  }

  public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
    ExecutorService executor = this.getThreadPoolExecutor();
    try {
      ListenableFutureTask<T> future = new ListenableFutureTask(task);
      executor.execute(future);
      return future;
    } catch (RejectedExecutionException rejectedExecutionException) {
      throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task,
          rejectedExecutionException);
    }
  }

  @Override
  protected ExecutorService initializeExecutor(ThreadFactory threadFactory,
      RejectedExecutionHandler rejectedExecutionHandler) {
    BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueSize);
    ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, timeout,
        TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler) {
      protected void beforeExecute(Thread t, Runnable r) {
        timeCache.put(String.valueOf(r.hashCode()), System.currentTimeMillis());
        super.beforeExecute(t, r);
      }

      protected void afterExecute(Runnable r, Throwable throwable) {
        Long startTime = timeCache.remove(String.valueOf(r.hashCode()));
        long duration = System.currentTimeMillis() - startTime;
        if (duration > MAX_SHOW_SPEND_TIME) {
          int activeCount = WindyThreadPool.this.getActiveCount();
          int coreSize = WindyThreadPool.this.getCorePoolSize();
          int largestPoolSize = WindyThreadPool.this.getMaxPoolSize();
          long completedTaskCount = WindyThreadPool.this.getCompletedTaskCount();
          long taskCount = WindyThreadPool.this.getTaskCount();
          int queueSize = WindyThreadPool.this.getQueueSize();
          WindyThreadPool.log.info(
              "duration:{},corePoolSize:{},activeCount:{},completed:{},taskCount:{},queue:{},largestPoolSize:{}",
              duration, coreSize, activeCount, completedTaskCount, taskCount, queueSize,
              largestPoolSize);
          log.warn("pool runnable run time more than max time");
        }
        if (Objects.nonNull(throwable)) {
          throwable.printStackTrace();
        }
      }
    };
    if (this.allowCoreThreadTimeOut) {
      executor.allowCoreThreadTimeOut(true);
    }

    this.executor = executor;
    return executor;
  }

  public long getCompletedTaskCount() {
    return Objects.isNull(executor) ? 0L : executor.getCompletedTaskCount();
  }

  public long getTaskCount() {
    return Objects.isNull(executor) ? 0L : executor.getTaskCount();
  }

  public int getQueueSize() {
    return Objects.isNull(executor) ? 0 : executor.getQueue().size();
  }

  public int getActiveCount() {
    return Objects.isNull(executor) ? 0 : executor.getActiveCount();
  }

  public void setCorePoolSize(Integer corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  public void setMaxPoolSize(Integer maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }

  public void setTimeout(Long timeout) {
    this.timeout = timeout;
  }


  public void setQueueSize(Integer queueSize) {
    this.queueSize = queueSize;
  }

  public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
    this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
  }
}
