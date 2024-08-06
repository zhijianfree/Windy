package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.demand.BusinessStatusDTO;
import com.zj.domain.entity.enums.BusinessStatusType;
import com.zj.domain.entity.po.demand.BusinessStatus;
import com.zj.domain.mapper.demand.BusinessStatusMapper;
import com.zj.domain.repository.demand.IBusinessStatusRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class IBusinessStatusRepositoryImpl extends ServiceImpl<BusinessStatusMapper, BusinessStatus> implements IBusinessStatusRepository {
    @Override
    public List<BusinessStatusDTO> getDemandStatuses() {
        return getStatusListByType(BusinessStatusType.DEMAND);
    }

    @Override
    public List<BusinessStatusDTO> getBugStatuses() {
        return getStatusListByType(BusinessStatusType.BUG);
    }

    @Override
    public List<BusinessStatusDTO> getWorkTaskStatuses() {
        return getStatusListByType(BusinessStatusType.WORK);
    }

    private List<BusinessStatusDTO> getStatusListByType(BusinessStatusType bug) {
        List<BusinessStatus> list = list(Wrappers.lambdaQuery(BusinessStatus.class).eq(BusinessStatus::getType,
                bug.name()));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return OrikaUtil.convertList(list, BusinessStatusDTO.class);
    }
}
