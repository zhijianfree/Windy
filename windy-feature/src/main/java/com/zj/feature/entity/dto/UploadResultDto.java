package com.zj.feature.entity.dto;

import com.zj.client.loader.FeatureDefine;
import java.util.List;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/7/14
 */
@Data
public class UploadResultDto {

  private String fileName;

  private List<List<FeatureDefine>> templateDefines;
}
