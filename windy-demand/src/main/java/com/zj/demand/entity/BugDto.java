package com.zj.demand.entity;

import com.zj.domain.entity.vo.Create;
import com.zj.domain.entity.vo.Update;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class BugDto {

    /**
     * bug名称
     * */
    @Length(min = 10)
    @NotBlank(message = "缺陷名称不能为空", groups = Create.class)
    private String bugName;

    /**
     * bug的ID
     * */
    @NotBlank(groups = Update.class)
    private String bugId;

    /**
     * bug发生环境
     * */
    private String environment;

    /**
     * bug现象
     * */
    private String scene;

    /**
     * 重现步骤
     * */
    private String replayStep;

    /**
     * 期待结果
     * */
    private String expectResult;

    /**
     * 实际结果
     * */
    private String realResult;

    /**
     * 提出人名称
     * */
    private String proposerName;

    /**
     * 提出人
     * */
    private String proposer;

    /**
     * 接受人名称
     * */
    private String acceptorName;

    /**
     *
     * 接受人*/
    private String acceptor;

    /**
     * bug处理开始时间
     * */
    private Long startTime;

    /**
     * 标签
     * */
    private String tags;

    /**
     * bug级别
     * */
    @NotNull(groups = Create.class)
    private Integer level;

    /**
     * bug状态
     * */
    private Integer status;

    /**
     * 需求ID
     */
    @NotBlank(message = "缺陷关联的需求ID不能为空",groups = Create.class)
    private String demandId;

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
     * */
    private Long createTime;

    /**
     * 更新时间
     * */
    private Long updateTime;
}
