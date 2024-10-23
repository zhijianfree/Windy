package com.zj.auth.service;

import com.zj.auth.entity.GroupTree;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.auth.GroupDto;
import com.zj.domain.repository.auth.IGroupRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final IGroupRepository groupRepository;
    private final UniqueIdService uniqueIdService;

    public GroupService(IGroupRepository groupRepository, UniqueIdService uniqueIdService) {
        this.groupRepository = groupRepository;
        this.uniqueIdService = uniqueIdService;
    }


    public boolean createGroup(GroupDto groupDto) {
        if (StringUtils.isNotBlank(groupDto.getParentId())) {

        }
        groupDto.setGroupId(uniqueIdService.getUniqueId());
        return groupRepository.createGroup(groupDto);
    }

    public List<GroupTree> getGroups() {
        List<GroupDto> groups = groupRepository.getGroups();
        GroupTree rootTree = new GroupTree();
        convertTree(OrikaUtil.convertList(groups, GroupTree.class), rootTree);
        return rootTree.getChildren();
    }

    private void convertTree(List<GroupTree> featureList, GroupTree parent) {
        if (CollectionUtils.isEmpty(featureList)) {
            return;
        }

        List<GroupTree> list = featureList.stream()
                .filter(feature -> Objects.equals(feature.getParentId(), parent.getGroupId()))
                .collect(Collectors.toList());
        parent.setChildren(list);

        featureList.removeIf(feature -> Objects.equals(feature.getParentId(), parent.getGroupId()));
        list.forEach(node -> convertTree(featureList, node));
    }

    public boolean updateGroup(String groupId, GroupDto groupDto) {
        groupDto.setGroupId(groupId);
        return groupRepository.updateGroup(groupDto);
    }

    public boolean deleteGroup(String groupId) {
        return groupRepository.deleteGroup(groupId);
    }

    public GroupDto getGroup(String groupId) {
        return groupRepository.getGroup(groupId);
    }
}
