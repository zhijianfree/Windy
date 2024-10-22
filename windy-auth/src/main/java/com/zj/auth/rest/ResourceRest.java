package com.zj.auth.rest;

import com.zj.auth.entity.ResourceBind;
import com.zj.auth.service.ResourceService;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.auth.ResourceDto;
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
public class ResourceRest {

    private final ResourceService resourceService;

    public ResourceRest(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/resources")
    public ResponseMeta<PageSize<ResourceDto>> getResources(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                           @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, resourceService.getResources(page,size));
    }

    @GetMapping("/auth/menus")
    public ResponseMeta<List<ResourceDto>> getUserMenuList() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, resourceService.getUserMenuList());
    }

    @GetMapping("/resource/all")
    public ResponseMeta<List<ResourceDto>> getAllResources() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, resourceService.getAllResources());
    }
    
    @PostMapping("/resources")
    public ResponseMeta<Boolean> createResource(@RequestBody ResourceDto resourceDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, resourceService.createResource(resourceDto));
    }

    @GetMapping("/roles/{roleId}/resources")
    public ResponseMeta<List<ResourceDto>> getRoleResources(@PathVariable("roleId") String roleId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, resourceService.getRoleResources(roleId));
    }

    @PostMapping("/resource/bind")
    public ResponseMeta<Boolean> resourceBind(@RequestBody ResourceBind resourceBind) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, resourceService.resourceBind(resourceBind));
    }

    @PutMapping("/resources/{resourceId}")
    public ResponseMeta<Boolean> updateResource(@PathVariable("resourceId") String resourceId, @RequestBody ResourceDto resourceDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, resourceService.updateResource(resourceId, resourceDto));
    }

    @DeleteMapping("/resources/{resourceId}")
    public ResponseMeta<Boolean> deleteResource(@PathVariable("resourceId") String resourceId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, resourceService.deleteResource(resourceId));
    }

    @GetMapping("/resources/{resourceId}")
    public ResponseMeta<ResourceDto> getResource(@PathVariable("resourceId") String resourceId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, resourceService.getResource(resourceId));
    }

}
