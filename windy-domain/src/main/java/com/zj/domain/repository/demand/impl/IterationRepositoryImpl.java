package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.domain.entity.po.demand.Iteration;
import com.zj.domain.mapper.demand.IterationMapper;
import com.zj.domain.repository.demand.IterationRepository;
import org.springframework.stereotype.Repository;

@Repository
public class IterationRepositoryImpl extends ServiceImpl<IterationMapper, Iteration> implements
    IterationRepository {

}
