package com.zj.domain.repository.service;

import com.zj.common.entity.generate.GenerateRecordBO;
import java.util.List;

public interface IGenerateRecordRepository {

  /**
   * 获取服务二方包生成记录
   * @param serviceId 服务ID
   * @return 生成记录列表
   */
  List<GenerateRecordBO> getServiceRecords(String serviceId);

  /**
   * 创建二方包生成记录
   * @param generateRecordBO 生成记录
   * @return 是否成功
   */
  boolean create(GenerateRecordBO generateRecordBO);

  /**
   * 更新二方包生成记录
   * @param generateRecordBO 生成记录
   * @return 是否成功
   */
  boolean update(GenerateRecordBO generateRecordBO);

  /**
   * 获取生成记录
   * @param serviceId 服务ID
   * @param version 版本
   * @return 生成记录列表
   */
  List<GenerateRecordBO> getGenerateRecord(String serviceId, String version);
}
