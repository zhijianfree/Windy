package com.zj.pipeline.entity.dto;

import com.zj.common.entity.dto.ClientCollect;
import com.zj.common.entity.dto.MasterCollect;
import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Data
public class SystemMonitorDto {

  private List<ClientCollect> clients;

  private List<MasterCollect> masters;
}
