package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.BugQueryBO;
import com.zj.domain.entity.enums.BugStatus;
import com.zj.domain.entity.po.demand.Bug;
import com.zj.domain.mapper.demand.BugMapper;
import com.zj.domain.repository.demand.IBugRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BugRepositoryImpl extends ServiceImpl<BugMapper, Bug> implements IBugRepository {

    @Override
    public PageSize<BugBO> getUserBugs(BugQueryBO bugQueryBO) {
        LambdaQueryWrapper<Bug> wrapper = Wrappers.lambdaQuery(Bug.class);
        Optional.ofNullable(bugQueryBO.getStatus()).ifPresent(status -> wrapper.eq(Bug::getStatus, status));
        if (StringUtils.isNotBlank(bugQueryBO.getProposer())) {
            wrapper.eq(Bug::getProposer, bugQueryBO.getProposer());
        }
        if (StringUtils.isNotBlank(bugQueryBO.getIterationId())) {
            wrapper.eq(Bug::getIterationId, bugQueryBO.getIterationId());
        }
        if (StringUtils.isNotBlank(bugQueryBO.getSpaceId())) {
            wrapper.eq(Bug::getSpaceId, bugQueryBO.getSpaceId());
        }
        if (StringUtils.isNotBlank(bugQueryBO.getName())) {
            wrapper.eq(Bug::getBugName, bugQueryBO.getName());
        }
        if (StringUtils.isNotBlank(bugQueryBO.getAcceptor())) {
            wrapper.eq(Bug::getAcceptor, bugQueryBO.getAcceptor());
        }
        if (Objects.isNull(bugQueryBO.getStatus())) {
            wrapper.in(Bug::getStatus,
                    BugStatus.getNotHandleBugs().stream().map(BugStatus::getType).collect(Collectors.toList()));
        }
        wrapper.orderByDesc(Bug::getCreateTime);
        IPage<Bug> pageQuery = new Page<>(bugQueryBO.getPage(), bugQueryBO.getSize());
        return exchangePageSize(pageQuery, wrapper);
    }

    @Override
    public PageSize<BugBO> getUserRelatedBugs(BugQueryBO bugQueryBO) {
        LambdaQueryWrapper<Bug> wrapper = Wrappers.lambdaQuery(Bug.class).eq(Bug::getAcceptor,
                bugQueryBO.getProposer());
        Optional.ofNullable(bugQueryBO.getStatus()).ifPresent(status -> wrapper.eq(Bug::getStatus, status));
        if (Objects.isNull(bugQueryBO.getStatus())) {
            wrapper.in(Bug::getStatus,
                    BugStatus.getNotHandleBugs().stream().map(BugStatus::getType).collect(Collectors.toList()));
        }
        IPage<Bug> pageQuery = new Page<>(bugQueryBO.getPage(), bugQueryBO.getSize());
        return exchangePageSize(pageQuery, wrapper);
    }

    private PageSize<BugBO> exchangePageSize(IPage<Bug> pageQuery, LambdaQueryWrapper<Bug> wrapper) {
        IPage<Bug> bugPage = page(pageQuery, wrapper);
        PageSize<BugBO> pageSize = new PageSize<>();
        pageSize.setTotal(bugPage.getTotal());
        if (CollectionUtils.isNotEmpty(bugPage.getRecords())) {
            pageSize.setData(OrikaUtil.convertList(bugPage.getRecords(), BugBO.class));
        }
        return pageSize;
    }

    @Override
    public boolean createBug(BugBO bugBO) {
        Bug bug = OrikaUtil.convert(bugBO, Bug.class);
        bug.setStatus(BugStatus.NOT_HANDLE.getType());
        bug.setCreateTime(System.currentTimeMillis());
        bug.setUpdateTime(System.currentTimeMillis());
        return save(bug);
    }

    @Override
    public boolean updateBug(BugBO bugBO) {
        Bug bug = OrikaUtil.convert(bugBO, Bug.class);
        bug.setUpdateTime(System.currentTimeMillis());
        return update(bug, Wrappers.lambdaUpdate(Bug.class).eq(Bug::getBugId, bug.getBugId()));
    }

    @Override
    public BugBO getBug(String bugId) {
        Bug bug = getOne(Wrappers.lambdaQuery(Bug.class).eq(Bug::getBugId, bugId));
        if (Objects.isNull(bug)) {
            return null;
        }
        return OrikaUtil.convert(bug, BugBO.class);
    }

    @Override
    public boolean deleteBug(String bugId) {
        return remove(Wrappers.lambdaQuery(Bug.class).eq(Bug::getBugId, bugId));
    }

    @Override
    public Integer countIteration(String iterationId) {
        return count(Wrappers.lambdaQuery(Bug.class).eq(Bug::getIterationId, iterationId));
    }

    @Override
    public List<BugBO> getIterationBugs(String iterationId) {
        List<Bug> list = list(Wrappers.lambdaQuery(Bug.class).eq(Bug::getIterationId, iterationId));
        return OrikaUtil.convertList(list, BugBO.class);
    }

    @Override
    public List<BugBO> getBugsByName(String queryName) {
        List<Bug> list = list(Wrappers.lambdaQuery(Bug.class).like(Bug::getBugName, queryName));
        return OrikaUtil.convertList(list, BugBO.class);
    }

    @Override
    public List<BugBO> getSpaceNotHandleBugs(String spaceId) {
        List<Bug> list = list(Wrappers.lambdaQuery(Bug.class).eq(Bug::getSpaceId, spaceId).in(Bug::getStatus,
                BugStatus.getNotHandleBugs().stream().map(BugStatus::getType).collect(Collectors.toList())));
        return OrikaUtil.convertList(list, BugBO.class);
    }

    @Override
    public List<BugBO> getIterationNotHandleBugs(String iterationId) {
        List<Bug> list = list(Wrappers.lambdaQuery(Bug.class).eq(Bug::getIterationId, iterationId).in(Bug::getStatus,
                BugStatus.getNotHandleBugs().stream().map(BugStatus::getType).collect(Collectors.toList())));
        return OrikaUtil.convertList(list, BugBO.class);
    }

    @Override
    public List<BugBO> getNotCompleteBugs(List<String> bugIds) {
        List<Bug> list = list(Wrappers.lambdaQuery(Bug.class).in(Bug::getBugId, bugIds).in(Bug::getStatus,
                BugStatus.getNotHandleBugs().stream().map(BugStatus::getType).collect(Collectors.toList())));
        return OrikaUtil.convertList(list, BugBO.class);
    }

    @Override
    public boolean batchUpdateStatus(List<String> bugIds, int status) {
        if (CollectionUtils.isEmpty(bugIds)) {
            return false;
        }
        UpdateWrapper<Bug> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("status", status).in("bug_id", bugIds);
        return baseMapper.update(null, updateWrapper) > 0;
    }
}
