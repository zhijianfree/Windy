package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.demand.IterationDTO;
import com.zj.domain.entity.po.demand.Iteration;
import com.zj.domain.mapper.demand.IterationMapper;
import com.zj.domain.repository.demand.IterationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IterationRepositoryImpl extends ServiceImpl<IterationMapper, Iteration> implements IterationRepository {

    @Override
    public List<IterationDTO> getIterationList() {
        List<Iteration> iterations = list();
        return OrikaUtil.convertList(iterations, IterationDTO.class);
    }

    @Override
    public IterationDTO createIteration(IterationDTO iterationDTO) {
        iterationDTO.setCreateTime(System.currentTimeMillis());
        iterationDTO.setUpdateTime(System.currentTimeMillis());
        Iteration iteration = OrikaUtil.convert(iterationDTO, Iteration.class);
        return save(iteration) ? iterationDTO : null;
    }

    @Override
    public IterationDTO getIteration(String iterationId) {
        Iteration iteration = getOne(Wrappers.lambdaQuery(Iteration.class).eq(Iteration::getIterationId, iterationId));
        return OrikaUtil.convert(iteration, IterationDTO.class);
    }

    @Override
    public boolean deleteIteration(String iterationId) {
        return remove(Wrappers.lambdaQuery(Iteration.class).eq(Iteration::getIterationId, iterationId));
    }

    @Override
    public boolean updateIteration(IterationDTO iterationDTO) {
        iterationDTO.setUpdateTime(System.currentTimeMillis());
        Iteration iteration = OrikaUtil.convert(iterationDTO, Iteration.class);
        return update(iteration, Wrappers.lambdaQuery(Iteration.class).eq(Iteration::getIterationId, iteration.getIterationId()));
    }
}
