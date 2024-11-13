package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.demand.IterationBO;
import com.zj.domain.entity.bo.service.ResourceMemberBO;
import com.zj.domain.entity.enums.IterationStatus;
import com.zj.domain.entity.po.demand.Iteration;
import com.zj.domain.mapper.demand.IterationMapper;
import com.zj.domain.repository.demand.IMemberRepository;
import com.zj.domain.repository.demand.IterationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class IterationRepositoryImpl extends ServiceImpl<IterationMapper, Iteration> implements IterationRepository {

    private final IMemberRepository memberRepository;

    public IterationRepositoryImpl(IMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public List<IterationBO> getIterationList(String spaceId, List<String> iterationIds) {
        List<Iteration> iterations = list(Wrappers.lambdaQuery(Iteration.class).in(Iteration::getIterationId,
                        iterationIds)
                .eq(Iteration::getSpaceId, spaceId).orderByDesc(Iteration::getCreateTime));
        return OrikaUtil.convertList(iterations, IterationBO.class);
    }

    @Override
    public List<IterationBO> getSpaceNotHandleIterations(String spaceId) {
        List<Iteration> iterations = list(Wrappers.lambdaQuery(Iteration.class).eq(Iteration::getSpaceId, spaceId)
                .in(Iteration::getStatus, IterationStatus.getNotHandleIterations().stream().map(IterationStatus::getType)
                        .collect(Collectors.toList())));
        return OrikaUtil.convertList(iterations, IterationBO.class);
    }

    @Override
    @Transactional
    public IterationBO createIteration(IterationBO iterationBO) {
        iterationBO.setCreateTime(System.currentTimeMillis());
        iterationBO.setUpdateTime(System.currentTimeMillis());
        Iteration iteration = OrikaUtil.convert(iterationBO, Iteration.class);
        boolean result = save(iteration);
        if (result) {
            ResourceMemberBO resourceMemberBO = new ResourceMemberBO();
            resourceMemberBO.setUserId(iterationBO.getUserId());
            resourceMemberBO.setResourceId(iterationBO.getIterationId());
            memberRepository.addResourceMember(resourceMemberBO);
        }
        return result ? iterationBO : null;
    }

    @Override
    public IterationBO getIteration(String iterationId) {
        Iteration iteration = getOne(Wrappers.lambdaQuery(Iteration.class).eq(Iteration::getIterationId, iterationId));
        return OrikaUtil.convert(iteration, IterationBO.class);
    }

    @Override
    public boolean deleteIteration(String iterationId) {
        return remove(Wrappers.lambdaQuery(Iteration.class).eq(Iteration::getIterationId, iterationId));
    }

    @Override
    public boolean updateIteration(IterationBO iterationBO) {
        iterationBO.setUpdateTime(System.currentTimeMillis());
        Iteration iteration = OrikaUtil.convert(iterationBO, Iteration.class);
        return update(iteration, Wrappers.lambdaQuery(Iteration.class).eq(Iteration::getIterationId,
                iteration.getIterationId()));
    }
}
