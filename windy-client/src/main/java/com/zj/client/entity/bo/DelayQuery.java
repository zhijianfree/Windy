package com.zj.client.entity.bo;

import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import lombok.Data;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class DelayQuery implements Delayed {

    private TaskNode taskNode;

    private AtomicLong expire;

    private AtomicBoolean cancel;

    public DelayQuery() {
        this.expire = new AtomicLong(-1);
        this.cancel = new AtomicBoolean(false);
    }

    public DelayQuery(TaskNode taskNode, long seconds) {
        this.taskNode = taskNode;
        this.expire = new AtomicLong(System.currentTimeMillis() + seconds * 1000);
        this.cancel = new AtomicBoolean(false);
    }

    public void setExpire(Long expire) {
        this.expire.set(expire);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long delay = expire.get() - System.currentTimeMillis();
        return unit.convert(delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof DelayQuery){
            return Long.compare(expire.get(), ((DelayQuery) o).getExpire().get());
        }
        return 0;
    }
}
