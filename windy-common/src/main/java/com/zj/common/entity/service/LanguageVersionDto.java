package com.zj.common.entity.service;

import lombok.Data;

import java.util.List;

@Data
public class LanguageVersionDto {

    /**
     * java版本列表
     */
    private List<String> javaVersions;

    /**
     * go语言版本列表
     */
    private List<String> goVersions;
}
