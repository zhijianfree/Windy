package com.zj.domain.entity.po.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("windy_group")
public class Group {

    private Long id;

    /**
     * 组织ID
     */
    private String groupId;

    /**
     * 组织名称
     */
    private String groupName;

    /**
     * 组织描述
     */
    private String description;

    private String parentId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}
