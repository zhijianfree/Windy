package com.zj.domain.repository.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.po.auth.User;
import com.zj.domain.mapper.auth.UserMapper;
import com.zj.domain.repository.auth.IUserRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements IUserRepository {


    @Override
    public UserBO getUserByUserName(String userName) {
        User one = getOne(Wrappers.lambdaQuery(User.class).eq(User::getUserName, userName));
        return OrikaUtil.convert(one, UserBO.class);
    }

    @Override
    public UserBO getUserByUserId(String userId) {
        User one = getOne(Wrappers.lambdaQuery(User.class).eq(User::getUserId, userId));
        return OrikaUtil.convert(one, UserBO.class);
    }


    @Override
    public PageSize<UserBO> getGroupUserPage(String groupId, Integer page, Integer size) {
        LambdaQueryWrapper<User> wrapper =
                Wrappers.lambdaQuery(User.class).eq(User::getGroupId, groupId).orderByDesc(User::getCreateTime);
        IPage<User> pageQuery = new Page<>(page, size);
        return exchangePageSize(pageQuery, wrapper);
    }

    @Override
    public List<UserBO> getGroupUserList(String groupId) {
        List<User> list = list(Wrappers.lambdaQuery(User.class).eq(User::getGroupId, groupId).orderByDesc(User::getCreateTime));
        return OrikaUtil.convertList(list, UserBO.class);
    }

    private PageSize<UserBO> exchangePageSize(IPage<User> pageQuery, LambdaQueryWrapper<User> wrapper) {
        IPage<User> bugPage = page(pageQuery, wrapper);
        PageSize<UserBO> pageSize = new PageSize<>();
        pageSize.setTotal(bugPage.getTotal());
        if (CollectionUtils.isNotEmpty(bugPage.getRecords())) {
            pageSize.setData(OrikaUtil.convertList(bugPage.getRecords(), UserBO.class));
        }
        return pageSize;
    }

    @Override
    public Boolean createUser(UserBO userBO) {
        User user = OrikaUtil.convert(userBO, User.class);
        user.setSalt(UUID.randomUUID().toString().replace("-",""));
        user.setUpdateTime(System.currentTimeMillis());
        user.setCreateTime(System.currentTimeMillis());
        return save(user);
    }

    @Override
    public Boolean updateUser(UserBO userBO) {
        User user = OrikaUtil.convert(userBO, User.class);
        user.setUpdateTime(System.currentTimeMillis());
        return update(user, Wrappers.lambdaQuery(User.class).eq(User::getUserId, userBO.getUserId()));
    }

    @Override
    public Boolean deleteUser(String userId) {
        return remove(Wrappers.lambdaQuery(User.class).eq(User::getUserId, userId));
    }

    @Override
    public List<UserBO> getUserByUserList(List<String> userIds) {
        List<User> users = list(Wrappers.lambdaQuery(User.class).in(User::getUserId, userIds));
        return OrikaUtil.convertList(users, UserBO.class);
    }

    @Override
    public List<UserBO> fuzzyQueryUserName(String name) {
        List<User> users = list(Wrappers.lambdaQuery(User.class).like(User::getUserName, name));
        return OrikaUtil.convertList(users, UserBO.class);
    }
}
