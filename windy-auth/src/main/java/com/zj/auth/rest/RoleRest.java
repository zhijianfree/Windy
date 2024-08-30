package com.zj.auth.rest;

import com.zj.auth.entity.RoleBind;
import com.zj.auth.service.RoleService;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.auth.RoleDto;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/devops")
public class RoleRest {

    private final RoleService roleService;

    public RoleRest(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public ResponseMeta<PageSize<RoleDto>> getRoles(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                           @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, roleService.getRoles(page,size));
    }

    @GetMapping("/role/all")
    public ResponseMeta<List<RoleDto>> getAllRoles() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, roleService.getAllRoles());
    }

    @PostMapping("/role/bind")
    public ResponseMeta<Boolean> bindRole(@RequestBody RoleBind roleBind) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, roleService.bindRole(roleBind));
    }

    @PostMapping("/roles")
    public ResponseMeta<Boolean> createRole(@RequestBody RoleDto roleDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, roleService.createRole(roleDto));
    }

    @PutMapping("/roles/{roleId}")
    public ResponseMeta<Boolean> updateRole(@PathVariable("roleId") String roleId, @RequestBody RoleDto roleDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, roleService.updateRole(roleId, roleDto));
    }

    @DeleteMapping("/roles/{roleId}")
    public ResponseMeta<Boolean> deleteRole(@PathVariable("roleId") String roleId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, roleService.deleteRole(roleId));
    }

    @GetMapping("/roles/{roleId}")
    public ResponseMeta<RoleDto> getRole(@PathVariable("roleId") String roleId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, roleService.getRole(roleId));
    }

    @GetMapping("/groups/{groupId}/roles")
    public ResponseMeta<PageSize<RoleDto>> getGroupRoles(@PathVariable("groupId") String groupId,
                                                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                         @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, roleService.getGroupRoles(groupId, page, size));
    }


}
