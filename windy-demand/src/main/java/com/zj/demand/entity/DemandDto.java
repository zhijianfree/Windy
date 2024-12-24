package com.zj.demand.entity;

import com.zj.domain.entity.vo.Create;
import com.zj.domain.entity.vo.Update;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DemandDto {

    /**
     * 需求Id
     */
    @NotBlank(groups = Update.class)
    private String demandId;

    /**
     * 需求名称
     */
    @Length(min = 10, max = 50)
    @NotBlank(groups = Create.class)
    private String demandName;

    /**
     * 需求描述
     */
    @Length(max = 1000)
    private String demandContent;

    /**
     * 客户价值
     */
    @Length(max = 40)
    private String customerValue;

    /**
     * 接受人名称
     */
    @Length(max = 64)
    private String acceptorName;

    /**
     * 接受人
     */
    @Length(max = 64)
    @NotBlank(message = "需求接受人不能为空", groups = Create.class)
    private String acceptor;

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
    @NotNull(groups = Create.class)
    private Integer level;

    /**
     * 期待完成时间
     */
    @NotNull(groups = Create.class, message = "需求期望完成时间不能为空")
    private Long expectTime;

    /**
     * 开始研发时间
     */
    private Long startTime;

    /**
     * 工作量
     */
    @NotNull(groups = Create.class, message = "需求工作量不能为空")
    private Double workload;

    /**
     * 需求标签
     * */
    private String tag;

    /**
     * 空间ID
     */
    private String spaceId;

    /**
     * 迭代ID
     */
    private String iterationId;
}
