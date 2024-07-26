package com.zj.client.handler.feature.ability.wait;

import com.zj.plugin.loader.ExecuteDetailVo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WaitInvoker {
    public ExecuteDetailVo waitTimeout(Long timeout){
        ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
        executeDetailVo.addRequestInfo("wait time(s)", timeout);
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            countDownLatch.await(timeout, TimeUnit.SECONDS);
        }catch (InterruptedException e) {
        }
        executeDetailVo.setStatus(true);
        Map<String, String> res = new HashMap<>();
        res.put("execute", "done");
        executeDetailVo.setResBody(res);
        return executeDetailVo;
    }
}
