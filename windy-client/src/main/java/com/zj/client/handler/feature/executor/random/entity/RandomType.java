package com.zj.client.handler.feature.executor.random.entity;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public enum RandomType {
    RANDOM_STRING("RandomString"),
    RANDOM_INTEGER("RandomInteger");

    private final String type;

    RandomType(String type) {
        this.type = type;
    }

    public static RandomEntity exchangeRandomType(String randomString) {
        // 正则表达式匹配 $FunctionName(params)
        Pattern pattern = Pattern.compile("\\$(\\w+)\\((.*?)\\)");
        Matcher matcher = pattern.matcher(randomString);
        if (matcher.find()) {
            String functionName = matcher.group(1);  // 获取函数名
            String params = matcher.group(2);        // 获取括号内的参数
            // 根据函数名调用对应的随机生成方法
            switch (functionName) {
                case "RandomString":
                    Integer length = Optional.ofNullable(params).filter(StringUtils::isNoneBlank).map(Integer::parseInt).orElse(6);
                    return RandomEntity.builder().randomType(RandomType.RANDOM_STRING).randomRule(new StringRandomRule(length)).build();
                case "RandomInteger":
                    String range = Optional.ofNullable(params).filter(StringUtils::isNoneBlank).orElse("1,10");
                    String[] rangeArray = range.split(",");
                    int min = Integer.parseInt(rangeArray[0]);
                    int max = Integer.parseInt(rangeArray[1]);
                    return RandomEntity.builder().randomType(RandomType.RANDOM_INTEGER).randomRule(new IntegerRandomRule(max, min)).build();
                default:
                    return null;
            }
        }
        return null;
    }
}
