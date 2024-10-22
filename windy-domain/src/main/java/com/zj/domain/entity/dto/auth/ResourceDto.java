package com.zj.domain.entity.dto.auth;

import lombok.Data;

@Data
public class ResourceDto {

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
     * 资源操作符
     */
    private String operate;

    /**
     * 资源类型
     */
    private Integer resourceType;

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
