package com.zj.client.entity.vo;

import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.plugin.loader.ExecuteDetailVo;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FeatureResponse {
    /**
     * 执行的详细信息
     */
    private ExecuteDetailVo executeDetailVo;

    private CompareResult compareResult;

    /**
     * 执行过程中需要设置的临时全局变量
     */
    private Map<String, Object> context;

    /**
     * 模版名称
     */
    private String name;

    /**
     * 执行点Id
     */
    private String pointId;

    public boolean isSuccess() {
        boolean invokeStatus = true;
        if (Objects.nonNull(executeDetailVo)) {
            invokeStatus = Optional.ofNullable(executeDetailVo.responseStatus()).orElse(false);
        }

        boolean compareStatus = true;
        if (Objects.nonNull(compareResult)){
            compareStatus = compareResult.isCompareSuccess();
        }

        return invokeStatus && compareStatus;
    }
}
