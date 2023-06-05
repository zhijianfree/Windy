package com.zj.client.feature.executor.compare;

import com.zj.client.entity.vo.ExecuteDetailVo;
import com.zj.client.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.common.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
public class CompareHandler {

  public static final String RESPONSE_CODE = "$code";
  public static final String RESPONSE_BODY = "$body";
  public static final String TIP_FORMAT = "compare key[%s] expect value is[%s] but find [%s]";
  private OgnlDataParser ognlDataParser = new OgnlDataParser();
  private final Map<String, CompareOperator> compareOperators;

  public CompareHandler(List<CompareOperator> compareOperators) {
    this.compareOperators = compareOperators.stream().collect(Collectors.toMap(
        compareOperator -> compareOperator.getType().getOperator(),
        compareOperator -> compareOperator));
  }

  public CompareResult compare(ExecuteDetailVo executeDetailVo, List<CompareDefine> compareDefines) {
    CompareResult compareResult = new CompareResult();
    compareResult.setCompareStatus(true);
    if (CollectionUtils.isEmpty(compareDefines)) {
      log.warn("compare defines is empty");
      return compareResult;
    }

    compareDefines = compareDefines.stream().filter(Objects::nonNull).filter(
        compare -> StringUtils.isNoneBlank(compare.getCompareKey()) && StringUtils.isNoneBlank(
            compare.getExpectValue())).collect(
        Collectors.toList());

    preHandle(executeDetailVo, compareDefines);

    for (CompareDefine compareDefine : compareDefines) {
      compareResult = compareOne(compareDefine);
      if (!compareResult.getCompareStatus()) {
        compareResult.setDescription(
            String.format(TIP_FORMAT, compareDefine.getCompareKey(), compareDefine.getExpectValue(),
                compareDefine.getResponseValue()));
        compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
        return compareResult;
      }
    }
    return compareResult;
  }

  private CompareResult compareOne(CompareDefine compareDefine) {
    CompareResult compareResult = new CompareResult();
    CompareOperator compareOperator = compareOperators.get(compareDefine.getOperator());
    if (Objects.isNull(compareOperator)) {
      compareResult.setCompareStatus(false);
      compareResult.setErrorMessage("not support operator [" + compareDefine.getOperator() + "]");
      return compareResult;
    }

    compareResult = compareOperator.compare(compareDefine);
    return compareResult;
  }

  /**
   * 预处理是将全局符替换成运行后的真实数据
   */
  private void preHandle(ExecuteDetailVo executeDetailVo, List<CompareDefine> compareDefines) {
    if (Objects.isNull(executeDetailVo.responseBody())) {
      return;
    }

    compareDefines.stream().filter(
        compare -> StringUtils.isNoneBlank(compare.getCompareKey()) && StringUtils.isNoneBlank(
            compare.getCompareKey())).forEach(compareDefine -> {
      String key = compareDefine.getCompareKey();
      if (Objects.equals(key, RESPONSE_CODE)) {
        compareDefine.setResponseValue(String.valueOf(executeDetailVo.responseStatus()));
        return;
      }

      if (Objects.equals(key, RESPONSE_BODY)) {
        compareDefine.setResponseValue(executeDetailVo.responseBody());
        return;
      }

      String compare = key.replace("$", "#");
      Object result = ognlDataParser.parserExpression(executeDetailVo.responseBody(), compare);
      compareDefine.setResponseValue(result);
    });
  }

}
