package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.DemandStatus;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.demand.DemandDTO;
import com.zj.domain.entity.dto.demand.DemandQuery;
import com.zj.domain.entity.po.demand.Demand;
import com.zj.domain.mapper.demand.DemandMapper;
import com.zj.domain.repository.demand.IDemandRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DemandRepositoryImpl extends ServiceImpl<DemandMapper, Demand> implements IDemandRepository {
    @Override
    public boolean createDemand(DemandDTO demandDTO) {
        Demand demand = OrikaUtil.convert(demandDTO, Demand.class);
        demand.setStatus(DemandStatus.CREATE.getType());
        demand.setCreateTime(System.currentTimeMillis());
        demand.setUpdateTime(System.currentTimeMillis());
        return save(demand);
    }

    @Override
    public PageSize<DemandDTO> getDemandPage(DemandQuery query) {
        IPage<Demand> pageObj = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<Demand> queryWrapper = Wrappers.lambdaQuery(Demand.class).eq(Demand::getCreator,
                        query.getCreator()).orderByDesc(Demand::getCreateTime);
        Optional.ofNullable(query.getStatus()).ifPresent(status -> queryWrapper.eq(Demand::getStatus, status));
        if (StringUtils.isNotBlank(query.getSpaceId())){
            queryWrapper.eq(Demand::getSpaceId, query.getSpaceId());
        }
        if (StringUtils.isNotBlank(query.getIterationId())){
            queryWrapper.eq(Demand::getIterationId, query.getIterationId());
        }
        if (StringUtils.isNotBlank(query.getName())){
            queryWrapper.like(Demand::getDemandName, query.getName());
        }
        IPage<Demand> recordPage = page(pageObj, queryWrapper);
        return convertPageSize(recordPage);
    }

    @Override
    public boolean updateDemand(DemandDTO demandDTO) {
        Demand demand = OrikaUtil.convert(demandDTO, Demand.class);
        demand.setUpdateTime(System.currentTimeMillis());
        return update(demand, Wrappers.lambdaUpdate(Demand.class).eq(Demand::getDemandId, demand.getDemandId()));
    }

    @Override
    public DemandDTO getDemand(String demandId) {
        Demand demand = getOne(Wrappers.lambdaUpdate(Demand.class).eq(Demand::getDemandId, demandId));
        return Optional.ofNullable(demand).map(d -> OrikaUtil.convert(d, DemandDTO.class)).orElse(null);
    }

    @Override
    public boolean deleteDemand(String demandId) {
        return remove(Wrappers.lambdaUpdate(Demand.class).eq(Demand::getDemandId, demandId));
    }

    @Override
    public PageSize<DemandDTO> getRelatedDemands(String proposer, Integer pageNo, Integer size) {
        IPage<Demand> pageObj = new Page<>(pageNo, size);
        IPage<Demand> recordPage = page(pageObj, Wrappers.lambdaQuery(Demand.class).eq(Demand::getProposer, proposer)
                .orderByDesc(Demand::getCreateTime));

        return convertPageSize(recordPage);
    }

    @Override
    public Integer countIteration(String iterationId) {
        return count(Wrappers.lambdaUpdate(Demand.class).eq(Demand::getIterationId, iterationId));
    }

    @Override
    public List<DemandDTO> getIterationDemand(String iterationId) {
        List<Demand> list = list(Wrappers.lambdaUpdate(Demand.class).eq(Demand::getIterationId, iterationId));
        return OrikaUtil.convertList(list, DemandDTO.class);
    }

    @Override
    public List<DemandDTO> getDemandsByName(String queryName) {
        List<Demand> list = list(Wrappers.lambdaUpdate(Demand.class).like(Demand::getDemandName, queryName));
        return OrikaUtil.convertList(list, DemandDTO.class);
    }

    private PageSize<DemandDTO> convertPageSize(IPage<Demand> recordPage) {
        List<DemandDTO> list = OrikaUtil.convertList(recordPage.getRecords(), DemandDTO.class);
        PageSize<DemandDTO> pageSize = new PageSize<>();
        pageSize.setTotal(recordPage.getTotal());
        pageSize.setData(list);
        return pageSize;
    }
}
