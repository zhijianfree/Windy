package com.zj.pipeline.entity.dto;

import com.zj.common.utils.OrikaUtil;
import com.zj.pipeline.entity.po.CodeChange;
import java.util.Objects;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author falcon
 * @since 2021/10/15
 */

@Data
public class CodeChangeDto {

    /**
     * 变更ID
     */
    private String changeId;

    /**
     * 变更名称
     */
    @NotEmpty
    private String changeName;

    /**
     * 变更描述
     */
    private String changeDesc;

    /**
     * 变更创建者
     */
//    @NotEmpty
    private String creator;

    /**
     * 变更分支
     */
    @NotEmpty
    private String changeBranch;

    /**
     * 服务Id
     */
    @NotEmpty
    private String serviceId;

    /**
     * 关联ID 每次的变更触发可以与需求或者是bug或者是一个优化项关联，通过这个关联的ID就可以在后续的代码工作中串联起来 达到观察工作流的作用
     * <p>
     * 同样关联ID也可以作为与第三方合作的打通ID
     */
    @NotEmpty
    private String relationId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    public static CodeChange toCodeChange(CodeChangeDto codeChangeDto) {
        CodeChange codeChange = new CodeChange();
        codeChange.setChangeId(codeChangeDto.getChangeId());
        codeChange.setChangeDesc(codeChangeDto.getChangeDesc());
        codeChange.setChangeName(codeChangeDto.getChangeName());
        codeChange.setChangeBranch(codeChangeDto.getChangeBranch());
        codeChange.setServiceId(codeChangeDto.getServiceId());
        codeChange.setCreator(codeChangeDto.getCreator());
        codeChange.setRelationId(codeChangeDto.getRelationId());
        return codeChange;
    }

    public static CodeChangeDto toCodeChangeDto(CodeChange codeChange) {
        if (Objects.isNull(codeChange)) {
            return null;
        }

        return OrikaUtil.convert(codeChange, CodeChangeDto.class);
    }
}
