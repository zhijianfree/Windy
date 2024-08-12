package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.domain.entity.po.demand.Space;
import com.zj.domain.mapper.demand.SpaceMapper;
import com.zj.domain.repository.demand.ISpaceRepository;
import org.springframework.stereotype.Repository;

@Repository
public class SpaceRepositoryImpl extends ServiceImpl<SpaceMapper, Space> implements ISpaceRepository {

}
