package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.demand.BusinessStatusDTO;
import com.zj.domain.entity.enums.BusinessStatusType;
import com.zj.domain.entity.po.demand.BusinessStatus;
import com.zj.domain.mapper.demand.BusinessStatusMapper;
import com.zj.domain.repository.demand.BusinessStatusRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class BusinessStatusRepositoryImpl extends ServiceImpl<BusinessStatusMapper, BusinessStatus> implements BusinessStatusRepository {
    @Override
    public List<BusinessStatusDTO> getDemandStatuses() {
        List<BusinessStatus> list = list(Wrappers.lambdaQuery(BusinessStatus.class).eq(BusinessStatus::getType,
                BusinessStatusType.DEMAND.name()));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return OrikaUtil.convertList(list, BusinessStatusDTO.class);
    }
}
