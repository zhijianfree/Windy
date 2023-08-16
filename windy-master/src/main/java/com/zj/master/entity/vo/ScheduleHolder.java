package com.zj.master.entity.vo;

import lombok.Data;

import java.util.concurrent.ScheduledFuture;

/**
 * @author falcon
 * @since 2023/7/20
 */
@Data
public class ScheduleHolder {

  private String cron;

  private ScheduledFuture scheduledFuture;
}
