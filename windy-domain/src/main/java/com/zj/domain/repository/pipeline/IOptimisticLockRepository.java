package com.zj.domain.repository.pipeline;

/**
 * @author falcon
 * @since 2023/7/19
 */
public interface IOptimisticLockRepository {

  /**
   * 是否有锁
   * @param bizCode 业务编码
   * @return 是否有锁
   */
  boolean hasLock(String bizCode);

  /**
   * 尝试加锁
   * @param bizCode 业务编码
   * @return 是否加锁成功
   */
  boolean tryLock(String bizCode);
}
