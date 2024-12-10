package com.zj.demand.rest;

import com.zj.common.entity.dto.PageSize;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.demand.entity.BugDetailDto;
import com.zj.demand.entity.BugDto;
import com.zj.demand.service.BugService;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.BusinessStatusBO;
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
    public ResponseMeta<BugDto> createBug(@Validated @RequestBody BugDto bugDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.createBug(bugDto));
    }

    @PutMapping("/bug")
    public ResponseMeta<Boolean> updateBug(@Validated @RequestBody BugDto bugDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.updateBug(bugDto));
    }

    @GetMapping("/bugs")
    public ResponseMeta<PageSize<BugBO>> getBugPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                    @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                    @RequestParam(value = "name", required = false) String name,
                                                    @RequestParam(value = "spaceId", required = false) String spaceId,
                                                    @RequestParam(value = "iterationId", required = false) String iterationId,
                                                    @RequestParam(value = "acceptor", required = false) String acceptor,
                                                    @RequestParam(value = "type", required = false) Integer type,
                                                    @RequestParam(value = "status", required = false) Integer status) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.getBugPage(page, size, name, status, spaceId,
                iterationId, acceptor, type));
    }

    @GetMapping("/bug/tags")
    public ResponseMeta<List<BusinessStatusBO>> getBugTags() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.getBugTags());
    }

    @GetMapping("/bugs/{bugId}")
    public ResponseMeta<BugDetailDto> getBug(@PathVariable("bugId") String bugId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.getBug(bugId));
    }

    @GetMapping("/iterations/{iterationId}/bugs")
    public ResponseMeta<List<BugBO>> getIterationBugs(@PathVariable("iterationId") String iterationId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.getIterationBugs(iterationId));
    }

    @GetMapping("/bug/statuses")
    public ResponseMeta<List<BusinessStatusBO>> getBugStatuses() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.getBugStatuses());
    }

    @DeleteMapping("/bugs/{bugId}")
    public ResponseMeta<Boolean> deleteBug(@PathVariable("bugId") String bugId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.deleteBug(bugId));
    }

    @GetMapping("/user/bugs")
    public ResponseMeta<PageSize<BugBO>> getRelatedBugs(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                        @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                        @RequestParam(value = "status", required = false) Integer status) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, bugService.getRelatedBugs(page, size, status));
    }

}
