package com.zj.domain.repository.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.auth.UserDto;
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
    public UserDto getUserByUserName(String userName) {
        User one = getOne(Wrappers.lambdaQuery(User.class).eq(User::getUserName, userName));
        return OrikaUtil.convert(one, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        User one = getOne(Wrappers.lambdaQuery(User.class).eq(User::getUserId, userId));
        return OrikaUtil.convert(one, UserDto.class);
    }


    @Override
    public PageSize<UserDto> getGroupUserPage(String groupId, Integer page, Integer size) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class).eq(User::getGroupId, groupId);
        IPage<User> pageQuery = new Page<>(page, size);
        return exchangePageSize(pageQuery, wrapper);
    }

    @Override
    public List<UserDto> getGroupUserList(String groupId) {
        List<User> list = list(Wrappers.lambdaQuery(User.class).eq(User::getGroupId, groupId));
        return OrikaUtil.convertList(list, UserDto.class);
    }

    private PageSize<UserDto> exchangePageSize(IPage<User> pageQuery, LambdaQueryWrapper<User> wrapper) {
        IPage<User> bugPage = page(pageQuery, wrapper);
        PageSize<UserDto> pageSize = new PageSize<>();
        pageSize.setTotal(bugPage.getTotal());
        if (CollectionUtils.isNotEmpty(bugPage.getRecords())) {
            pageSize.setData(OrikaUtil.convertList(bugPage.getRecords(), UserDto.class));
        }
        return pageSize;
    }

    @Override
    public Boolean createUser(UserDto userDto) {
        User user = OrikaUtil.convert(userDto, User.class);
        user.setSalt(UUID.randomUUID().toString().replace("-",""));
        user.setUpdateTime(System.currentTimeMillis());
        user.setCreateTime(System.currentTimeMillis());
        return save(user);
    }

    @Override
    public Boolean updateUser(UserDto userDto) {
        User user = OrikaUtil.convert(userDto, User.class);
        user.setUpdateTime(System.currentTimeMillis());
        return update(user, Wrappers.lambdaQuery(User.class).eq(User::getUserId, userDto.getUserId()));
    }

    @Override
    public Boolean deleteUser(String userId) {
        return remove(Wrappers.lambdaQuery(User.class).eq(User::getUserId, userId));
    }

    @Override
    public List<UserDto> getUserByUserList(List<String> userIds) {
        List<User> users = list(Wrappers.lambdaQuery(User.class).in(User::getUserId, userIds));
        return OrikaUtil.convertList(users, UserDto.class);
    }

    @Override
    public List<UserDto> fuzzyQueryUserName(String name) {
        List<User> users = list(Wrappers.lambdaQuery(User.class).like(User::getUserName, name));
        return OrikaUtil.convertList(users, UserDto.class);
    }
}
