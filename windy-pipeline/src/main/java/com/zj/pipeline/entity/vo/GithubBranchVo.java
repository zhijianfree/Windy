package com.zj.pipeline.entity.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class GithubBranchVo {

    private String name;

    @JSONField(name = "protected")
    private Boolean isProtected;
}
