package com.zj.client.handler.feature.executor.random;

import com.zj.client.handler.feature.executor.random.entity.RandomEntity;
import com.zj.client.handler.feature.executor.random.entity.RandomRule;
import com.zj.client.handler.feature.executor.random.entity.RandomType;

public interface IRandomGenerator<T extends RandomRule> {

    RandomType randomType();

    Object generateRandom(T randomRule);
}
