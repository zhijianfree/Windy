package com.zj.plugin.loader;

import java.util.List;

public interface Feature {

    /**
     * 此接口用于扫描所有模版接口功能呢
     * @return 模版功能描述列表
     */
    List<FeatureDefine> scanFeatureDefines();

    /**
     * Windy自定义的模版主要有两大类:同步模版和异步模版
     * 同步模版: 模版是阻塞式运行，模版执行之后同步返回执行结果
     * 异步模版: 模版是异步线程执行，模版执行之后无法立即返回结果，需要等待一定周期后才可获取到结果
     *
     * 此方法只在模版需要异步执行时使用，异步线程执行是通过竞争到Cpu时间来执行的，那么执行的时间点比较随机，
     * 如果在用例中存在多个模版，异步模版需要捕捉同步模版的执行结果，那么就一定需要异步模版进入"等待监听"状态之后才可执行同步模版
     * 否则无法监听到同步模版执行的结果。异步模版进入"等待监听"状态之后，通过IAsyncNotifyListener通知给Windy，然后Windy才会执行下一个
     * 模版，以此来保证异步模版一定可捕获同步模版的执行结果。
     * @param listener 异步通知监听
     */
    default void bindListener(IAsyncNotifyListener listener) {
    }
}
