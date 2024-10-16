package com.zj.demand.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseMeta;
import com.zj.demand.service.BugService;
import com.zj.domain.entity.dto.demand.BugDTO;
import com.zj.domain.entity.dto.demand.BusinessStatusDTO;
import org.springframework.validation.annotation.Validated;
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
public class BugRest {
    private final BugService bugService;

    public BugRest(BugService bugService) {
        this.bugService = bugService;
    }

    @PostMapping("/bugs")
    public ResponseMeta<BugDTO> createBug(@Validated @RequestBody BugDTO bugDTO) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.createBug(bugDTO));
    }

    @PutMapping("/bug")
    public ResponseMeta<Boolean> updateBug(@Validated @RequestBody BugDTO bugDTO) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.updateBug(bugDTO));
    }

    @GetMapping("/bugs")
    public ResponseMeta<PageSize<BugDTO>> getBugPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                     @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                     @RequestParam(value = "name", required = false) String name,
                                                     @RequestParam(value = "status", required = false) Integer status) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.getBugPage(page, size, name, status));
    }

    @GetMapping("/bugs/{bugId}")
    public ResponseMeta<BugDTO> getBug(@PathVariable("bugId") String bugId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.getBug(bugId));
    }

    @GetMapping("/bug/statuses")
    public ResponseMeta<List<BusinessStatusDTO>> getBugStatuses() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.getBugStatuses());
    }

    @DeleteMapping("/bugs/{bugId}")
    public ResponseMeta<Boolean> deleteBug(@PathVariable("bugId") String bugId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.deleteBug(bugId));
    }

    @GetMapping("/user/bugs")
    public ResponseMeta<PageSize<BugDTO>> getRelatedBugs(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                         @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                         @RequestParam(value = "status", required = false) Integer status) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.getRelatedBugs(page, size, status));
    }

}
