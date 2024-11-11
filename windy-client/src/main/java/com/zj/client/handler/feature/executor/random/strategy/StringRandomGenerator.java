package com.zj.client.handler.feature.executor.random.strategy;

import com.zj.client.handler.feature.executor.random.IRandomGenerator;
import com.zj.client.handler.feature.executor.random.entity.RandomType;
import com.zj.client.handler.feature.executor.random.entity.StringRandomRule;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class StringRandomGenerator implements IRandomGenerator<StringRandomRule> {
    @Override
    public Object generateRandom(StringRandomRule randomRule) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < randomRule.getLength(); i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }

    @Override
    public RandomType randomType() {
        return RandomType.RANDOM_STRING;
    }
}
