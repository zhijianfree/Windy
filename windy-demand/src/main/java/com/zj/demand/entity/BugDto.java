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
    @Length(min = 4, max = 100)
    @NotBlank(message = "缺陷名称不能为空", groups = Create.class)
    private String bugName;

    /**
     * bug的ID
     * */
    @NotBlank(groups = Update.class, message = "缺陷ID不能为空")
    private String bugId;

    /**
     * bug发生环境
     * */
    @Length(max = 100)
    private String environment;

    /**
     * bug现象
     * */
    @Length(max = 1000)
    private String scene;

    /**
     * 重现步骤
     * */
    @Length(max = 100)
    private String replayStep;

    /**
     * 期待结果
     * */
    @Length(max = 100)
    private String expectResult;

    /**
     * 实际结果
     * */
    @Length(max = 100)
    private String realResult;

    /**
     * 接受人名称
     * */
    @Length(max = 100)
    private String acceptorName;

    /**
     *
     * 接受人*/
    @Length(max = 64)
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
    private String relationId;

    /**
     * 空间ID
     */
    @Length(max = 64)
    private String spaceId;

    /**
     * 迭代ID
     */
    @Length(max = 64)
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
