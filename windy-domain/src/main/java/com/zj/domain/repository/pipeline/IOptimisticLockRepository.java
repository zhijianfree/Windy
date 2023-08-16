package com.zj.domain.repository.pipeline;

/**
 * @author falcon
 * @since 2023/7/19
 */
public interface IOptimisticLockRepository {

  boolean hasLock(String bizCode);

  boolean tryLock(String bizCode);
}
