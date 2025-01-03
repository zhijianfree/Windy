package com.zj.client.handler.feature.executor.compare.operator;

import com.zj.common.entity.feature.CompareDefine;
import com.zj.common.entity.feature.CompareResult;
import com.zj.common.enums.CompareType;
import com.zj.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class NotEmptyCompare extends BaseCompare{
    @Override
    public CompareType getType() {
        return CompareType.NOT_EMPTY;
    }

    @Override
    public CompareResult compare(CompareDefine compareDefine) {
        CompareResult compareResult = createSuccessResult();
        log.info("get resp value={}", compareDefine.getResponseValue());
        if (Objects.isNull(compareDefine.getResponseValue()) ||
                StringUtils.isBlank(String.valueOf(compareDefine.getResponseValue()))) {
            compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
            compareResult.setErrorMessage("response value is empty");
        }
        return compareResult;
    }
}
