package com.zj.client.handler.feature.executor.random.strategy;

import com.zj.client.handler.feature.executor.random.IRandomGenerator;
import com.zj.client.handler.feature.executor.random.entity.IntegerRandomRule;
import com.zj.client.handler.feature.executor.random.entity.RandomType;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class IntegerRandomGenerator implements IRandomGenerator<IntegerRandomRule> {
    @Override
    public Object generateRandom(IntegerRandomRule randomRule) {
        Integer max = randomRule.getMax();
        Integer min = randomRule.getMin();
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    @Override
    public RandomType randomType() {
        return RandomType.RANDOM_INTEGER;
    }

}
