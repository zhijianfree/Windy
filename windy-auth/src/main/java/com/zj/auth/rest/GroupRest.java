package com.zj.auth.rest;


import com.zj.auth.entity.GroupTree;
import com.zj.auth.entity.GroupUserTree;
import com.zj.auth.service.GroupService;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.domain.entity.bo.auth.GroupBO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/devops")
public class GroupRest {

    private final GroupService groupService;

    public GroupRest(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/groups")
    public ResponseMeta<List<GroupTree>> getGroups() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, groupService.getGroups());
    }

    @GetMapping("/group/user/tree")
    public ResponseMeta<List<GroupUserTree>> getGroupUserTree() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, groupService.getGroupUserTree());
    }

    @PostMapping("/groups")
    public ResponseMeta<Boolean> createGroup(@RequestBody GroupBO groupBO) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, groupService.createGroup(groupBO));
    }

    @PutMapping("/groups/{groupId}")
    public ResponseMeta<Boolean> updateGroup(@PathVariable("groupId") String groupId, @RequestBody GroupBO groupBO) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, groupService.updateGroup(groupId, groupBO));
    }

    @DeleteMapping("/groups/{groupId}")
    public ResponseMeta<Boolean> deleteGroup(@PathVariable("groupId") String groupId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, groupService.deleteGroup(groupId));
    }

    @GetMapping("/groups/{groupId}")
    public ResponseMeta<GroupBO> getGroup(@PathVariable("groupId") String groupId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, groupService.getGroup(groupId));
    }
}
