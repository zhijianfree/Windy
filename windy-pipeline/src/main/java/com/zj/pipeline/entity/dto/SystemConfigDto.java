package com.zj.pipeline.entity.dto;

import com.zj.pipeline.entity.po.NodeParamConfig;
import com.zj.pipeline.entity.po.SystemConfig;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Objects;

@Data
public class SystemConfigDto {
    /**
     * 配置Id
     * */
    private String configId;

    /**
     * 配置名称
     * */
    private String configName;

    /**
     * 父节点Id
     * */
    private String parentId;

    /**
     * 配置类型
     * */
    private String type;

    /**
     * 配置信息
     * */
    private String configDetail;

    /**
     * 排序
     * */
    private Integer sort;

    /**
     * 创建时间
     * */
    private Long createTime;

    /**
     * 修改时间
     * */
    private Long updateTime;

    public static SystemConfigDto toSystemConfigDto(SystemConfig systemConfig){
        if (Objects.isNull(systemConfig)){
            return null;
        }

        SystemConfigDto systemConfigDto = new SystemConfigDto();
        BeanUtils.copyProperties(systemConfig,systemConfigDto);
        return systemConfigDto;
    }

    public static SystemConfig toSystemConfig(SystemConfigDto systemConfigDto){
        SystemConfig systemConfig = new SystemConfig();
        BeanUtils.copyProperties(systemConfigDto,systemConfig);
        return systemConfig;
    }
}
