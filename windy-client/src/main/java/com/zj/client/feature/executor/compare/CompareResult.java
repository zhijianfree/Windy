package com.zj.client.feature.executor.compare;

import com.zj.common.exception.ErrorCode;
import lombok.Data;

@Data
public class CompareResult {
    private Boolean compareStatus;
    private String errorMessage;
    private String description;

    public void setErrorType(ErrorCode errorType){
        this.compareStatus = false;
        this.errorMessage = errorType.getMessage();
    }
}
