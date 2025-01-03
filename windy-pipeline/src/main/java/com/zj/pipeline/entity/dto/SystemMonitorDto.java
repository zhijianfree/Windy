package com.zj.pipeline.entity.dto;

import com.zj.common.entity.dto.ClientCollectDto;
import com.zj.common.entity.dto.MasterCollectDto;
import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Data
public class SystemMonitorDto {

  private List<ClientCollectDto> clients;

  private List<MasterCollectDto> masters;
}
