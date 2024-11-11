package com.zj.demand.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.demand.entity.IterationDto;
import com.zj.demand.entity.IterationStatisticDto;
import com.zj.demand.service.IterationService;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.demand.BusinessStatusBO;
import com.zj.domain.entity.bo.demand.IterationBO;
import com.zj.domain.entity.bo.service.ResourceMemberDto;
import com.zj.domain.entity.vo.Create;
import com.zj.domain.entity.vo.Update;
import org.springframework.validation.annotation.Validated;
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
public class IterationRest {
    private final IterationService iterationService;

    public IterationRest(IterationService iterationService) {
        this.iterationService = iterationService;
    }

    @GetMapping("/iterations/{iterationId}/statistic")
    public ResponseMeta<IterationStatisticDto> getIterationStatistic(@PathVariable("iterationId") String iterationId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.getIterationStatistic(iterationId));
    }

    @GetMapping("/{spaceId}/iterations")
    public ResponseMeta<List<IterationBO>> getSpaceIterationList(@PathVariable("spaceId") String spaceId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.getSpaceIterationList(spaceId));
    }

    @PostMapping("/iterations")
    public ResponseMeta<IterationBO> createIteration(@Validated(Create.class) @RequestBody IterationDto iterationDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.createIteration(iterationDto));
    }

    @PutMapping("/iterations/{iterationId}")
    public ResponseMeta<Boolean> updateIteration(@PathVariable("iterationId") String iterationId,
                                                 @Validated(Update.class) @RequestBody IterationDto iterationDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.updateIteration(iterationId, iterationDto));
    }

    @DeleteMapping("/iterations/{iterationId}")
    public ResponseMeta<Boolean> deleteIteration(@PathVariable("iterationId") String iterationId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.deleteIteration(iterationId));
    }

    @GetMapping("/iterations/{iterationId}")
    public ResponseMeta<IterationBO> getIteration(@PathVariable("iterationId") String iterationId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.getIteration(iterationId));
    }

    @GetMapping("/iterations/{iterationId}/members")
    public ResponseMeta<List<UserBO>> queryIterationMembers(@PathVariable("iterationId") String iterationId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.queryIterationMembers(iterationId));
    }

    @PostMapping("/iterations/{iterationId}/members")
    public ResponseMeta<Boolean> addIterationMember(@RequestBody ResourceMemberDto serviceMember) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.addIterationMember(serviceMember));
    }

    @DeleteMapping("/iterations/{iterationId}/members/{userId}")
    public ResponseMeta<Boolean> deleteIterationMember(@PathVariable("iterationId") String iterationId,
                                                     @PathVariable("userId") String userId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.deleteIterationMember(iterationId, userId));
    }

    @GetMapping("/iteration/statuses")
    public ResponseMeta<List<BusinessStatusBO>> getIterationStatuses() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.getIterationStatuses());
    }
}
