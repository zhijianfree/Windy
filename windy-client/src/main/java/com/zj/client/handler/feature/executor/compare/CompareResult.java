package com.zj.client.handler.feature.executor.compare;

import com.zj.common.exception.ErrorCode;
import lombok.Data;

@Data
public class CompareResult {
    private boolean compareStatus;
    private String errorMessage;
    private String description;

    public void setErrorType(ErrorCode errorType){
        this.compareStatus = false;
        this.errorMessage = errorType.getMessage();
    }
}
