package com.zj.client.handler.feature.executor.compare;

import com.zj.common.exception.ErrorCode;
import lombok.Data;

@Data
public class CompareResult {
    private boolean compareSuccess;
    private String errorMessage;
    private String description;

    public void setErrorType(ErrorCode errorType){
        this.compareSuccess = false;
        this.errorMessage = errorType.getMessage();
    }
}
