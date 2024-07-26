package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.po.pipeline.OptimisticLock;
import com.zj.domain.mapper.pipeline.OptimisticLockMapper;
import com.zj.domain.repository.pipeline.IOptimisticLockRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author falcon
 * @since 2023/7/19
 */
@Slf4j
@Repository
@EnableScheduling
@Scope(value = "singleton")
public class OptimisticLockRepository extends ServiceImpl<OptimisticLockMapper, OptimisticLock> implements IOptimisticLockRepository,
        DisposableBean {

    private Integer PERIOD_TIME = 1;
    private Integer BEFORE_PERIOD = 10 * 1000;
    private Map<String, OptimisticLock> lockMap = new HashMap<>();

    @Override
    public boolean hasLock(String bizCode) {
        OptimisticLock lock = lockMap.get(bizCode);
        if (Objects.isNull(lock)) {
            return false;
        }
        return Objects.equals(IpUtils.getLocalIP(), lock.getIp())
                && lock.getEndTime() > System.currentTimeMillis();
    }

    @Override
    public boolean tryLock(String bizCode) {
        try {
            if (hasLock(bizCode)) {
                return true;
            }

            OptimisticLock lock = getOptimisticLock(bizCode);
            if (Objects.isNull(lock)) {
                //如果不存在那么就创建一个新的lock
                //如果添加锁失败，证明数据库锁已存在
                return createLock(bizCode);
            }

            //锁如果存在那么就判断是否需要持有锁
            DateTime dateNow = new DateTime();
            long delta = lock.getEndTime() - dateNow.getMillis();
            if (delta > BEFORE_PERIOD) {
                return false;
            }

            boolean result = recoverNewLock(bizCode, lock, dateNow);
            log.info("lock bizCode={} result={}", bizCode, result);
            return result;
        } catch (Exception e) {
            log.error("lock Optimistic Lock error", e);
            return false;
        }
    }

    private boolean recoverNewLock(String bizCode, OptimisticLock lock, DateTime dateNow) {
        Long lockVersion = lock.getVersion();
        lock.setStartTime(dateNow.getMillis());
        lock.setEndTime(dateNow.plusHours(PERIOD_TIME).getMillis());
        lock.setIp(IpUtils.getLocalIP());
        lock.setNodeName(IpUtils.getHostName());
        lock.setVersion(lockVersion + 1);
        return update(lock,
                Wrappers.lambdaUpdate(OptimisticLock.class).eq(OptimisticLock::getBizCode, bizCode)
                        .eq(OptimisticLock::getVersion, lockVersion));
    }

    private boolean createLock(String bizCode) {
        OptimisticLock lock;
        lock = buildOptimisticLock(bizCode);
        try {
            boolean lockResult = save(lock);
            log.info("lock result={} bizCode={}", lockResult, bizCode);
            return lockResult;
        } catch (DuplicateKeyException e) {
            log.info("other node lock the bizCode={}", bizCode);
            return false;
        }
    }

    private OptimisticLock getOptimisticLock(String bizCode) {
        return getOne(
                Wrappers.lambdaQuery(OptimisticLock.class).eq(OptimisticLock::getBizCode, bizCode));
    }

    private OptimisticLock buildOptimisticLock(String bizCode) {
        OptimisticLock lock = new OptimisticLock();
        lock.setBizCode(bizCode);
        lock.setIp(IpUtils.getLocalIP());
        lock.setVersion(1L);
        lock.setNodeName(IpUtils.getHostName());

        DateTime dateTime = new DateTime();
        lock.setStartTime(dateTime.getMillis());
        lock.setEndTime(dateTime.plusHours(1).getMillis());
        return lock;
    }

    @Override
    public void destroy() throws Exception {
        List<String> lockKeys = lockMap.keySet().stream().filter(this::hasLock)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(lockKeys)) {
            return;
        }

        boolean clearResult = remove(
                Wrappers.lambdaQuery(OptimisticLock.class).in(OptimisticLock::getBizCode, lockKeys));
        log.info("clear biz code keys ={} result={}", lockKeys, clearResult);
    }
}
