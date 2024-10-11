package com.zj.domain.entity.po.auth;

import lombok.Data;

@Data
public class Resource {
    private Long id;

    /**
     * 资源Id
     * */
    private String resourceId;

    /**
     * 资源ID
     */
    private String resourceName;

    /**
     * 父节点ID
     */
    private String parentId;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 资源内容
     */
    private String content;

    /**
     * 操作类型
     */
    private String operate;

    /**
     * 是否可见
     */
    private boolean visible;

    /**
     * 资源icon
     */
    private String icon;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}