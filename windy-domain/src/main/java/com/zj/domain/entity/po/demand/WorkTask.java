package com.zj.domain.entity.po.demand;

import lombok.Data;

@Data
public class WorkTask {

    private Long id;

    /**
     * 工作任务Id
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 关联Id
     */
    private String relatedId;

    /**
     * 任务状态
     */
    private Integer status;

    /**
     * 工作量
     */
    private Integer workload;

    /**
     * 任务创建人
     */
    private String creator;

    /**
     * 完成时间
     */
    private Long completeTime;

    private Long createTime;

    private Long updateTime;
}
