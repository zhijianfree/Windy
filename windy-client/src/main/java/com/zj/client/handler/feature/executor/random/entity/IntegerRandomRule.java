package com.zj.client.handler.feature.executor.random.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IntegerRandomRule extends RandomRule{

    private Integer max;

    private Integer min;
}
