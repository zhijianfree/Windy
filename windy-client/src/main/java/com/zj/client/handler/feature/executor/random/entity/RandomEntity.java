package com.zj.client.handler.feature.executor.random.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RandomEntity<T extends RandomRule> {
    private RandomType randomType;
    private T randomRule;
}
