package com.zj.client.handler.feature.executor.random.strategy;

import com.zj.client.handler.feature.executor.random.IRandomGenerator;
import com.zj.client.handler.feature.executor.random.entity.RandomType;
import com.zj.client.handler.feature.executor.random.entity.StringRandomRule;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Random;

@Component
public class StringRandomGenerator implements IRandomGenerator<StringRandomRule> {

    private final Random random = new Random();
    @Override
    public Object generateRandom(StringRandomRule randomRule) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < randomRule.getLength(); i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }

        if (Objects.isNull(randomRule.getExchangeType())) {
            return result.toString();
        }

        if (Objects.equals(randomRule.getExchangeType(), 1)) {
            return result.toString().toUpperCase();
        }

        return result.toString().toLowerCase();
    }

    @Override
    public RandomType randomType() {
        return RandomType.RANDOM_STRING;
    }
}
