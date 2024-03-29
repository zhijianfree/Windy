package com.zj.client.handler.feature.executor.compare.operator;

import com.zj.client.handler.feature.executor.compare.CompareOperator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/6/14
 */
@Component
public class CompareFactory {

  private final Map<String, CompareOperator> compareOperators;

  public CompareFactory(List<CompareOperator> operators) {
    this.compareOperators = operators.stream().collect(Collectors.toMap(
        compareOperator -> compareOperator.getType().getOperator(),
        compareOperator -> compareOperator));
  }

  public CompareOperator getOperator(String operator) {
    return compareOperators.get(operator);
  }
}
