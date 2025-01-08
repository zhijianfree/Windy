package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.DemandQueryBO;
import com.zj.domain.entity.enums.DemandStatus;
import com.zj.domain.entity.po.demand.Demand;
import com.zj.domain.mapper.demand.DemandMapper;
import com.zj.domain.repository.demand.IDemandRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class DemandRepositoryImpl extends ServiceImpl<DemandMapper, Demand> implements IDemandRepository {

    public static final String DEMAND_STATUS_COLUMN_NAME = "status";
    public static final String DEMAND_ID_COLUMN_NAME = "demand_id";
    public static final String ACCEPT_TIME_COLUMN_NAME = "accept_time";
    public static final String UPDATE_TIME_COLUM_NAME = "update_time";

    @Override
    public boolean createDemand(DemandBO demandBO) {
        Demand demand = OrikaUtil.convert(demandBO, Demand.class);
        demand.setStatus(DemandStatus.NOT_HANDLE.getType());
        demand.setCreateTime(System.currentTimeMillis());
        demand.setUpdateTime(System.currentTimeMillis());
        return save(demand);
    }

    @Override
    public PageSize<DemandBO> getDemandPage(DemandQueryBO query) {
        IPage<Demand> pageObj = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<Demand> queryWrapper = Wrappers.lambdaQuery(Demand.class);
        Optional.ofNullable(query.getStatus()).ifPresent(status -> queryWrapper.eq(Demand::getStatus, status));
        if (StringUtils.isNotBlank(query.getSpaceId())) {
            queryWrapper.eq(Demand::getSpaceId, query.getSpaceId());
        }
        if (StringUtils.isNotBlank(query.getProposer())) {
            queryWrapper.eq(Demand::getProposer, query.getProposer());
        }

        if (StringUtils.isNotBlank(query.getIterationId())) {
            queryWrapper.eq(Demand::getIterationId, query.getIterationId());
        }
        if (StringUtils.isNotBlank(query.getName())) {
            queryWrapper.like(Demand::getDemandName, query.getName());
        }

        if (StringUtils.isNotBlank(query.getAcceptor())) {
            queryWrapper.like(Demand::getAcceptor, query.getAcceptor());
        }

        if (Objects.isNull(query.getStatus())) {
            queryWrapper.in(Demand::getStatus,
                    DemandStatus.getNotHandleDemands().stream().map(DemandStatus::getType).collect(Collectors.toList()));
        }
        queryWrapper.orderByDesc(Demand::getCreateTime);
        IPage<Demand> recordPage = page(pageObj, queryWrapper);
        return convertPageSize(recordPage);
    }

    @Override
    public boolean updateDemand(DemandBO demandBO) {
        Demand demand = OrikaUtil.convert(demandBO, Demand.class);
        demand.setUpdateTime(System.currentTimeMillis());
        return update(demand, Wrappers.lambdaUpdate(Demand.class).eq(Demand::getDemandId, demand.getDemandId()));
    }

    @Override
    public DemandBO getDemand(String demandId) {
        Demand demand = getOne(Wrappers.lambdaUpdate(Demand.class).eq(Demand::getDemandId, demandId));
        return Optional.ofNullable(demand).map(d -> OrikaUtil.convert(d, DemandBO.class)).orElse(null);
    }

    @Override
    public boolean deleteDemand(String demandId) {
        return remove(Wrappers.lambdaUpdate(Demand.class).eq(Demand::getDemandId, demandId));
    }

    @Override
    public List<DemandBO> getIterationDemand(String iterationId) {
        List<Demand> list =
                list(Wrappers.lambdaUpdate(Demand.class).eq(Demand::getIterationId, iterationId)
                        .orderByDesc(Demand::getCreateTime));
        return OrikaUtil.convertList(list, DemandBO.class);
    }

    @Override
    public List<DemandBO> getDemandsByFuzzName(String queryName) {
        List<Demand> list = list(Wrappers.lambdaUpdate(Demand.class).like(Demand::getDemandName, queryName));
        return OrikaUtil.convertList(list, DemandBO.class);
    }

    @Override
    public List<DemandBO> getSpaceNotHandleDemands(String spaceId) {
        List<Demand> list = list(Wrappers.lambdaUpdate(Demand.class).eq(Demand::getSpaceId, spaceId)
                .in(Demand::getStatus, DemandStatus.getNotHandleDemands().stream().map(DemandStatus::getType)
                        .collect(Collectors.toList())));
        return OrikaUtil.convertList(list, DemandBO.class);
    }

    @Override
    public List<DemandBO> getIterationNotHandleDemands(String iterationId) {
        List<Demand> list = list(Wrappers.lambdaUpdate(Demand.class).eq(Demand::getIterationId, iterationId)
                .in(Demand::getStatus, DemandStatus.getNotHandleDemands().stream().map(DemandStatus::getType)
                        .collect(Collectors.toList())));
        return OrikaUtil.convertList(list, DemandBO.class);
    }

    @Override
    public List<DemandBO> getNotCompleteDemandByIds(List<String> demandIds) {
        if (CollectionUtils.isEmpty(demandIds)) {
            return Collections.emptyList();
        }
        List<Demand> list = list(Wrappers.lambdaUpdate(Demand.class).in(Demand::getDemandId, demandIds)
                .in(Demand::getStatus, DemandStatus.getNotHandleDemands().stream().map(DemandStatus::getType)
                        .collect(Collectors.toList())));
        return OrikaUtil.convertList(list, DemandBO.class);
    }

    @Override
    public boolean batchUpdateStatus(List<String> demandIds, int status) {
        if (CollectionUtils.isEmpty(demandIds)) {
            log.info("batch update demand status demandIds is empty");
            return false;
        }
        UpdateWrapper<Demand> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(DEMAND_STATUS_COLUMN_NAME, status).in(DEMAND_ID_COLUMN_NAME, demandIds);
        return baseMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean batchUpdateProcessing(List<String> demandIds) {
        if (CollectionUtils.isEmpty(demandIds)) {
            log.info("batch update demand processing demandIds is empty");
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        UpdateWrapper<Demand> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(DEMAND_STATUS_COLUMN_NAME, DemandStatus.WORKING.getType())
                .set(ACCEPT_TIME_COLUMN_NAME, currentTimeMillis)
                .set(UPDATE_TIME_COLUM_NAME, currentTimeMillis)
                .in(DEMAND_ID_COLUMN_NAME, demandIds)
                .eq(DEMAND_STATUS_COLUMN_NAME, DemandStatus.NOT_HANDLE.getType());
        return baseMapper.update(null, updateWrapper) > 0;
    }

    private PageSize<DemandBO> convertPageSize(IPage<Demand> recordPage) {
        List<DemandBO> list = OrikaUtil.convertList(recordPage.getRecords(), DemandBO.class);
        PageSize<DemandBO> pageSize = new PageSize<>();
        pageSize.setTotal(recordPage.getTotal());
        pageSize.setData(list);
        return pageSize;
    }
}
