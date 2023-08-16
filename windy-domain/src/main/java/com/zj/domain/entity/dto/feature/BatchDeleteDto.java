package com.zj.domain.entity.dto.feature;

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
