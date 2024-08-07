package com.zj.domain.entity.po.demand;

import lombok.Data;

@Data
public class Demand {

    private Long id;

    /**
     * 需求Id
     * */
    private String demandId;

    /**
     * 需求名称
     * */
    private String demandName;

    /**
     * 需求描述
     * */
    private String demandContent;

    /**
     * 客户价值
     * */
    private String customerValue;

    /**
     * 附件地址
     * */
    private String enclosurePath;

    /**
     * 提出团队
     * */
    private String proposeTeam;

    /**
     * 提出人
     * */
    private String proposer;

    /**
     * 接受团队
     * */
    private String acceptTeam;

    /**
     * 接受人
     * */
    private String acceptor;

    /**
     * 接受时间
     * */
    private Long acceptTime;

    /**
     * 需求状态
     * */
    private Integer status;

    /**
     * 需求优先级
     */
    private Integer level;

    /**
     * 期待完成时间
     * */
    private Long expectTime;

    /**
     * 开始研发时间
     * */
    private Long startTime;

    /**
     * 工作量
     * */
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
     * 创建时间
     * */
    private Long createTime;

    /**
     * 修改时间
     * */
    private Long updateTime;
}
