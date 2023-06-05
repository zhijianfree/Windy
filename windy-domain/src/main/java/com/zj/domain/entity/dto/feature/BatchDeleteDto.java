package com.zj.domain.entity.dto.feature;

import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/1/30
 */
@Data
public class BatchDeleteDto {

  private List<String> features;
}
