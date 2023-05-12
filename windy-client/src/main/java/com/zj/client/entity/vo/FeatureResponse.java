package com.zj.client.entity.vo;

import com.zj.client.feature.executor.compare.CompareResult;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FeatureResponse {
    /**
     * 执行的详细信息
     */
    private ExecuteDetail executeDetail;

    private CompareResult compareResult;

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
        if (Objects.nonNull(executeDetail)) {
            invokeStatus = executeDetail.getResponseDetail().getResponseStatus();
        }

        boolean compareStatus = true;
        if (Objects.nonNull(compareResult)){
            compareStatus = compareResult.getCompareStatus();
        }

        return invokeStatus && compareStatus;
    }
}
