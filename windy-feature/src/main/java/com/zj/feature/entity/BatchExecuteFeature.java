package com.zj.feature.entity;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BatchExecuteFeature {

    /**
     * 批量更新的用例ID列表
     */
    @NotEmpty
    private List<@Valid @NotBlank String> featureIds;
}
