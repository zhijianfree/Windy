package com.zj.domain.repository.auth.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.domain.entity.po.auth.Resource;
import com.zj.domain.mapper.auth.ResourceMapper;
import com.zj.domain.repository.auth.IResourceRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceRepositoryImpl extends ServiceImpl<ResourceMapper, Resource> implements IResourceRepository {
}
