package com.zj.demand.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IterationStatistic {

    /**
     * 未完成需求个数
     */
    private Integer demandCount;

    /**
     * 未完成缺陷个数
     */
    private Integer bugCount;

    /**
     * 未完成任务个数
     */
    private Integer workCount;

    /**
     * 工作耗时
     */
    private Integer workload;
}
