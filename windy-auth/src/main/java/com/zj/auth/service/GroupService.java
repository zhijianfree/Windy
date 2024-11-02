package com.zj.auth.service;

import com.zj.auth.entity.GroupTree;
import com.zj.auth.entity.GroupUserTree;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.auth.GroupBO;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.repository.auth.IGroupRepository;
import com.zj.domain.repository.auth.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupService {

    private final IGroupRepository groupRepository;
    private final IUserRepository userRepository;
    private final UniqueIdService uniqueIdService;

    public GroupService(IGroupRepository groupRepository, IUserRepository userRepository, UniqueIdService uniqueIdService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.uniqueIdService = uniqueIdService;
    }


    public boolean createGroup(GroupBO groupBO) {
        groupBO.setGroupId(uniqueIdService.getUniqueId());
        return groupRepository.createGroup(groupBO);
    }

    public List<GroupTree> getGroups() {
        List<GroupBO> groups = groupRepository.getGroups();
        GroupTree rootTree = new GroupTree();
        convertTree(OrikaUtil.convertList(groups, GroupTree.class), rootTree);
        return rootTree.getChildren();
    }

    private void convertTree(List<GroupTree> groupTrees, GroupTree parent) {
        if (CollectionUtils.isEmpty(groupTrees)) {
            return;
        }

        List<GroupTree> list = groupTrees.stream()
                .filter(groupTree -> Objects.equals(groupTree.getParentId(), parent.getGroupId()))
                .collect(Collectors.toList());
        parent.setChildren(list);

        groupTrees.removeIf(groupTree -> Objects.equals(groupTree.getParentId(), parent.getGroupId()));
        list.forEach(node -> convertTree(groupTrees, node));
    }

    public boolean updateGroup(String groupId, GroupBO groupBO) {
        groupBO.setGroupId(groupId);
        return groupRepository.updateGroup(groupBO);
    }

    public boolean deleteGroup(String groupId) {
        return groupRepository.deleteGroup(groupId);
    }

    public GroupBO getGroup(String groupId) {
        return groupRepository.getGroup(groupId);
    }

    public List<GroupUserTree> getGroupUserTree() {
        List<GroupTree> groups = getGroups();
        GroupTree rootGroup = new GroupTree();
        rootGroup.setChildren(groups);
        GroupUserTree groupUserTree =  buildGroupUserTree(rootGroup);
        return groupUserTree.getChildren();
    }

    // 构建树的方法
    public GroupUserTree buildGroupUserTree(GroupTree groupTree) {
        // 构建当前组织节点
        GroupUserTree groupUserNode = new GroupUserTree();
        groupUserNode.setUserId(groupTree.getGroupId());
        groupUserNode.setName(groupTree.getGroupName());
        groupUserNode.setParentId(groupTree.getParentId());
        groupUserNode.setIsGroup(true); // 表示这是一个组织

        // 动态查询该组织的用户并加入树中
        List<GroupUserTree> userNodes = buildUserNodes(groupTree.getGroupId());
        groupUserNode.getChildren().addAll(userNodes);

        // 递归构建子组织的树结构
        if (groupTree.getChildren() != null) {
            for (GroupTree childGroup : groupTree.getChildren()) {
                GroupUserTree childNode = buildGroupUserTree(childGroup);
                groupUserNode.getChildren().add(childNode);
            }
        }

        return groupUserNode;
    }

    // 动态根据 groupId 调用接口获取用户，并构建用户节点
    private List<GroupUserTree> buildUserNodes(String groupId) {
        List<UserBO> users = userRepository.getGroupUserList(groupId); // 动态获取用户列表
        List<GroupUserTree> userNodes = new ArrayList<>();

        for (UserBO user : users) {
            GroupUserTree userNode = new GroupUserTree();
            userNode.setUserId(user.getUserId());
            String name =
                    Optional.ofNullable(user.getNickName()).filter(StringUtils::isNotBlank).orElseGet(user::getUserName);
            userNode.setName(name);
            userNode.setParentId(groupId);
            userNode.setIsGroup(false); // 表示这是一个用户
            userNodes.add(userNode);
        }
        return userNodes;
    }
}
