package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.demand.BugDTO;
import com.zj.domain.entity.dto.demand.BugQuery;
import com.zj.domain.entity.po.demand.Bug;
import com.zj.domain.mapper.demand.BugMapper;
import com.zj.domain.repository.demand.IBugRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
public class BugRepositoryImpl extends ServiceImpl<BugMapper, Bug> implements IBugRepository {

    @Override
    public PageSize<BugDTO> getUserBugs(BugQuery bugQuery) {
        LambdaQueryWrapper<Bug> wrapper = Wrappers.lambdaQuery(Bug.class).eq(Bug::getProposer, bugQuery.getUserId());
        Optional.ofNullable(bugQuery.getStatus()).ifPresent(status -> wrapper.eq(Bug::getStatus, status));
        if (StringUtils.isNotBlank(bugQuery.getName())){
            wrapper.eq(Bug::getBugName, bugQuery.getName());
        }
        IPage<Bug> pageQuery = new Page<>(bugQuery.getPage(), bugQuery.getSize());
        return exchangePageSize(pageQuery, wrapper);
    }

    @Override
    public PageSize<BugDTO> getUserRelatedBugs(BugQuery bugQuery) {
        LambdaQueryWrapper<Bug> wrapper = Wrappers.lambdaQuery(Bug.class).eq(Bug::getAcceptor, bugQuery.getUserId());
        Optional.ofNullable(bugQuery.getStatus()).ifPresent(status -> wrapper.eq(Bug::getStatus, status));
        IPage<Bug> pageQuery = new Page<>(bugQuery.getPage(), bugQuery.getSize());
        return exchangePageSize(pageQuery, wrapper);
    }

    private PageSize<BugDTO> exchangePageSize(IPage<Bug> pageQuery, LambdaQueryWrapper<Bug> wrapper) {
        IPage<Bug> bugPage = page(pageQuery, wrapper);
        PageSize<BugDTO> pageSize = new PageSize<>();
        pageSize.setTotal(bugPage.getTotal());
        if (CollectionUtils.isNotEmpty(bugPage.getRecords())) {
            pageSize.setData(OrikaUtil.convertList(bugPage.getRecords(), BugDTO.class));
        }
        return pageSize;
    }

    @Override
    public boolean createBug(BugDTO bugDTO) {
        Bug bug = OrikaUtil.convert(bugDTO, Bug.class);
        bug.setCreateTime(System.currentTimeMillis());
        bug.setUpdateTime(System.currentTimeMillis());
        return save(bug);
    }

    @Override
    public boolean updateBug(BugDTO bugDTO) {
        Bug bug = OrikaUtil.convert(bugDTO, Bug.class);
        bug.setUpdateTime(System.currentTimeMillis());
        return update(bug, Wrappers.lambdaUpdate(Bug.class).eq(Bug::getBugId, bug.getBugId()));
    }

    @Override
    public BugDTO getBug(String bugId) {
        Bug bug = getOne(Wrappers.lambdaQuery(Bug.class).eq(Bug::getBugId, bugId));
        if (Objects.isNull(bug)) {
            return null;
        }
        return OrikaUtil.convert(bug, BugDTO.class);
    }

    @Override
    public boolean deleteBug(String bugId) {
        return remove(Wrappers.lambdaQuery(Bug.class).eq(Bug::getBugId, bugId));
    }
}
