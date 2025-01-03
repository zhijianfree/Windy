package com.zj.client.handler.feature.executor.random.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StringRandomRule extends RandomRule{

    /**
     * 生成随机字符串长度
     */
    private Integer length;

    /**
     * 是否需要大小写转换
     */
    private Integer exchangeType;
}
