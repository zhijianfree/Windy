package com.zj.auth.service;

import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.auth.GroupDto;
import com.zj.domain.repository.auth.IGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    private final IGroupRepository groupRepository;
    private final UniqueIdService uniqueIdService;

    public GroupService(IGroupRepository groupRepository, UniqueIdService uniqueIdService) {
        this.groupRepository = groupRepository;
        this.uniqueIdService = uniqueIdService;
    }


    public boolean createGroup(GroupDto groupDto) {
        groupDto.setGroupId(uniqueIdService.getUniqueId());
        return groupRepository.createGroup(groupDto);
    }

    public List<GroupDto> getGroups() {
        return groupRepository.getGroups();
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
