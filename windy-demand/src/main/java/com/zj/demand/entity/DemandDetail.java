package com.zj.demand.entity;

import lombok.Data;

@Data
public class DemandDetail {

    /**
     * 需求Id
     */
    private String demandId;

    /**
     * 需求名称
     */
    private String demandName;

    /**
     * 需求描述
     */
    private String demandContent;

    /**
     * 客户价值
     */
    private String customerValue;

    /**
     * 附件地址
     */
    private String enclosurePath;

    /**
     * 提出人
     */
    private String proposer;

    /**
     * 提出人名称
     */
    private String proposerName;

    /**
     * 接受人
     */
    private String acceptor;

    /**
     * 接受人名称
     */
    private String acceptorName;

    /**
     * 接受时间
     */
    private Long acceptTime;

    /**
     * 需求状态
     */
    private Integer status;

    /**
     * 需求优先级
     */
    private Integer level;

    /**
     * 期待完成时间
     */
    private Long expectTime;

    /**
     * 开始研发时间
     */
    private Long startTime;

    /**
     * 工作量
     */
    private Double workload;

    /**
     * 需求标签
     * */
    private String tag;

    /**
     * 需求创建人
     */
    private String creator;

    /**
     * 空间ID
     */
    private String spaceId;

    /**
     * 迭代ID
     */
    private String iterationId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}
