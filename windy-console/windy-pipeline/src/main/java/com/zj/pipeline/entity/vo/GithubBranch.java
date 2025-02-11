package com.zj.pipeline.entity.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class GithubBranch {

    private String name;

    @JSONField(name = "protected")
    private Boolean isProtected;
}
