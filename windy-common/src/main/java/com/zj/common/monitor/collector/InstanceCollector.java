package com.zj.common.monitor.collector;

import com.zj.common.monitor.collector.PhysicsCollect.GarbageHistory;
import com.zj.common.utils.IpUtils;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 实例信息收集
 *
 * @author falcon
 * @since 2023/7/4
 */
public class InstanceCollector {
  private static DecimalFormat df = new DecimalFormat("#.00");

  public static PhysicsCollect collectPhysics() {
    PhysicsCollect physics = new PhysicsCollect();
    OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    // 获取 CPU 使用率
    double cpuUsage = osBean.getProcessCpuLoad() * 100;
    physics.setCpu(df.format(cpuUsage));

    // 获取物理内存使用率
    double physicalMemoryUsage =
        (osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize())
            / (double) osBean.getTotalPhysicalMemorySize() * 100;
    physics.setPhysicsCache(df.format(physicalMemoryUsage));

    // 获取线程管理器
    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    // 获取当前活动线程数
    int threadCount = threadBean.getThreadCount();
    physics.setThreads(threadCount);
    System.out.println("Thread Count: " + threadCount);

    // 获取内存管理器
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    // 获取堆内存使用情况
    MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
    double heapMemoryUsagePercentage =
        heapMemoryUsage.getUsed() / (double) heapMemoryUsage.getMax() * 100;
    physics.setHeap(df.format(heapMemoryUsagePercentage));

    // 获取非堆内存使用情况
    MemoryUsage nonHeapMemoryUsage = memoryBean.getNonHeapMemoryUsage();
    double nonHeapMemoryUsagePercentage =
        nonHeapMemoryUsage.getUsed() / (double) nonHeapMemoryUsage.getMax() * 100;
    physics.setNoHeap(df.format(nonHeapMemoryUsagePercentage));

    // 获取垃圾收集器信息
    List<GarbageHistory> histories = new ArrayList<>();
    List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    for (GarbageCollectorMXBean gcBean : gcBeans) {
      GarbageHistory garbageHistory = new GarbageHistory();
      garbageHistory.setCollector(gcBean.getName());
      garbageHistory.setCollectCount(gcBean.getCollectionCount());
      garbageHistory.setCollectTime(gcBean.getCollectionTime() + " ms");
      histories.add(garbageHistory);
    }
    physics.setHistories(histories);
    physics.setIp(IpUtils.getLocalIP());
    return physics;
  }
}
