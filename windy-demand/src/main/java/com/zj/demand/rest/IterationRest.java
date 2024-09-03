package com.zj.demand.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ResponseMeta;
import com.zj.demand.entity.IterationStatistic;
import com.zj.demand.service.IterationService;
import com.zj.domain.entity.dto.auth.UserDto;
import com.zj.domain.entity.dto.demand.IterationDTO;
import com.zj.domain.entity.dto.service.ResourceMemberDto;
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
    public ResponseMeta<IterationStatistic> getIterationStatistic(@PathVariable("iterationId") String iterationId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.getIterationStatistic(iterationId));
    }

    @GetMapping("/iterations")
    public ResponseMeta<List<IterationDTO>> getIterationList() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.getIterationList());
    }

    @PostMapping("/iterations")
    public ResponseMeta<IterationDTO> createIteration(@RequestBody IterationDTO iterationDTO) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.createIteration(iterationDTO));
    }

    @PutMapping("/iterations/{iterationId}")
    public ResponseMeta<Boolean> updateIteration(@PathVariable("iterationId") String iterationId, @RequestBody IterationDTO iterationDTO) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.updateIteration(iterationId, iterationDTO));
    }

    @DeleteMapping("/iterations/{iterationId}")
    public ResponseMeta<Boolean> deleteIteration(@PathVariable("iterationId") String iterationId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.deleteIteration(iterationId));
    }

    @GetMapping("/iterations/{iterationId}")
    public ResponseMeta<IterationDTO> getIteration(@PathVariable("iterationId") String iterationId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, iterationService.getIteration(iterationId));
    }

    @GetMapping("/iterations/{iterationId}/members")
    public ResponseMeta<List<UserDto>> queryIterationMembers(@PathVariable("iterationId") String iterationId) {
        return new ResponseMeta<List<UserDto>>(ErrorCode.SUCCESS, iterationService.queryIterationMembers(iterationId));
    }

    @PostMapping("/iterations/{iterationId}/members")
    public ResponseMeta<Boolean> addIterationMember(@RequestBody ResourceMemberDto serviceMember) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, iterationService.addIterationMember(serviceMember));
    }

    @DeleteMapping("/iterations/{iterationId}/members/{userId}")
    public ResponseMeta<Boolean> deleteIterationMember(@PathVariable("iterationId") String iterationId,
                                                     @PathVariable("userId") String userId) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, iterationService.deleteIterationMember(iterationId,
                userId));
    }
}
