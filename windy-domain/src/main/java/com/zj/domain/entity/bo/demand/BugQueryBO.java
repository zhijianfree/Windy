package com.zj.domain.entity.bo.demand;

import com.zj.domain.entity.vo.BaseQuery;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BugQueryBO extends BaseQuery {

    /**
     * 分页号
     */
    private Integer page;

    /**
     * 分页大小
     */
    private Integer size;

    /**
     * 缺陷名称
     */
    private String name;

    /**
     * 缺陷状态
     */
    private Integer status;

    /**
     * 空间ID
     */
    private String spaceId;

    /**
     * 迭代ID
     */
    private String iterationId;

    /**
     * 处理人
     */
    private String acceptor;

    /**
     * 用户ID
     */
    private String proposer;
}
