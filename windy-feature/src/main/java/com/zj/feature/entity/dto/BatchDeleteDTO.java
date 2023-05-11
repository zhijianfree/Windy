package com.zj.feature.entity.dto;

import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/1/30
 */
@Data
public class BatchDeleteDTO {

  private List<String> features;
}
