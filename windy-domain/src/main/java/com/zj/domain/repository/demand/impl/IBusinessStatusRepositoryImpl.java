package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.demand.BusinessStatusBO;
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
    public List<BusinessStatusBO> getDemandStatuses() {
        return getStatusListByType(BusinessStatusType.DEMAND);
    }

    @Override
    public List<BusinessStatusBO> getIterationStatuses() {
        return getStatusListByType(BusinessStatusType.ITERATION);
    }

    @Override
    public List<BusinessStatusBO> getBugStatuses() {
        return getStatusListByType(BusinessStatusType.BUG);
    }

    @Override
    public List<BusinessStatusBO> getWorkTaskStatuses() {
        return getStatusListByType(BusinessStatusType.WORK);
    }

    @Override
    public List<BusinessStatusBO> getDemandTags() {
        return getStatusListByType(BusinessStatusType.DEMAND_TAG);
    }

    @Override
    public List<BusinessStatusBO> getBugTags() {
        return getStatusListByType(BusinessStatusType.BUG_TAG);
    }

    private List<BusinessStatusBO> getStatusListByType(BusinessStatusType bug) {
        List<BusinessStatus> list = list(Wrappers.lambdaQuery(BusinessStatus.class).eq(BusinessStatus::getType,
                bug.name()));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return OrikaUtil.convertList(list, BusinessStatusBO.class);
    }
}
