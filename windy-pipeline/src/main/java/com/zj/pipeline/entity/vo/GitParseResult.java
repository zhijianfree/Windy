package com.zj.pipeline.entity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Data
@Builder
public class GitParseResult {

  private String repository;

  private String branch;
}
