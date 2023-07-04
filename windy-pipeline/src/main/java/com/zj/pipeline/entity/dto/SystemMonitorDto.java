package com.zj.pipeline.entity.dto;

import com.zj.common.model.ClientCollect;
import com.zj.common.model.MasterCollect;
import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Data
public class SystemMonitorDto {

  private List<ClientCollect> clients;

  private List<MasterCollect> masters;
}
