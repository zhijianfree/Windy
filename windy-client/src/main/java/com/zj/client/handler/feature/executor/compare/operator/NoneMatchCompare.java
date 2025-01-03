package com.zj.client.handler.feature.executor.compare.operator;

import com.zj.common.entity.feature.CompareDefine;
import com.zj.common.entity.feature.CompareResult;
import com.zj.common.enums.CompareType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoneMatchCompare extends BaseCompare {
    private final ArrayMatchCompare arrayMatchCompare;


    public NoneMatchCompare(ArrayMatchCompare arrayMatchCompare) {
        this.arrayMatchCompare = arrayMatchCompare;
    }

    @Override
    public CompareType getType() {
        return CompareType.NONE_ITEM_MATCH;
    }

    @Override
    public CompareResult compare(CompareDefine compareDefine) {
        CompareResult compareResult = arrayMatchCompare.compare(compareDefine);
        if (compareResult.isCompareSuccess()) {
            compareResult.setErrorMessage("array find item");
            compareResult.setCompareSuccess(false);
        } else {
            compareResult.setErrorMessage("");
            compareResult.setCompareSuccess(true);
        }
        return compareResult;
    }

}
