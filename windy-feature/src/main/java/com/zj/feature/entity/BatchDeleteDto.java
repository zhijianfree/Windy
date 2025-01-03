package com.zj.feature.entity;

import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/1/30
 */
@Data
public class BatchDeleteDto {

  private List<String> features;
}
