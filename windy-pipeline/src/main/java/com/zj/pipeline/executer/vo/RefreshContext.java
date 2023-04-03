package com.zj.pipeline.executer.vo;

import com.zj.pipeline.entity.vo.CompareResult;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * @author falcon
 * @since 2022/6/15
 */
@Data
@Builder
public class RefreshContext {

  private String url;

  private Map<String, String> headers;

  private List<CompareResult> compareConfig;
}
