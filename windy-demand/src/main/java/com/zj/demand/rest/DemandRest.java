package com.zj.demand.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseMeta;
import com.zj.demand.entity.DemandDetail;
import com.zj.demand.service.DemandService;
import com.zj.domain.entity.dto.demand.BusinessStatusDto;
import com.zj.domain.entity.dto.demand.DemandDTO;
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
public class DemandRest {

    private final DemandService demandService;

    public DemandRest(DemandService demandService) {
        this.demandService = demandService;
    }

    @PostMapping("/demands")
    public ResponseMeta<DemandDTO> createDemand(@RequestBody DemandDTO demandDTO) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.createDemand(demandDTO));
    }

    @PutMapping("/demand")
    public ResponseMeta<Boolean> updateDemand(@RequestBody DemandDTO demandDTO) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.updateDemand(demandDTO));
    }

    @GetMapping("/demands")
    public ResponseMeta<PageSize<DemandDTO>> getDemandPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                           @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                           @RequestParam(value = "name", required = false) String name,
                                                           @RequestParam(value = "spaceId", required = false) String spaceId,
                                                           @RequestParam(value = "iterationId", required = false) String iterationId,
                                                           @RequestParam(value = "status", required = false) Integer status) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getDemandPage(page, size, name, status, spaceId, iterationId));
    }

    @GetMapping("/iterations/{iterationId}/demands")
    public ResponseMeta<List<DemandDTO>> getIterationDemands(@PathVariable("iterationId") String iterationId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getIterationDemands(iterationId));
    }

    @GetMapping("/user/demands")
    public ResponseMeta<PageSize<DemandDTO>> getUserDemands(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                            @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                            @RequestParam(value = "status", required = false) Integer status) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getUserDemands(page, size, status));
    }

    @GetMapping("/demand/tags")
    public ResponseMeta<List<BusinessStatusDto>> getDemandTags() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getDemandTags());
    }

    @GetMapping("/demand/statuses")
    public ResponseMeta<List<BusinessStatusDto>> getDemandStatuses() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getDemandStatuses());
    }

    @GetMapping("/demands/{demandId}")
    public ResponseMeta<DemandDetail> getDemand(@PathVariable("demandId") String demandId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getDemand(demandId));
    }

    @DeleteMapping("/demands/{demandId}")
    public ResponseMeta<Boolean> deleteDemand(@PathVariable("demandId") String demandId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.deleteDemand(demandId));
    }

    @GetMapping("/related/demands")
    public ResponseMeta<PageSize<DemandDTO>> getRelatedDemands(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getRelatedDemands(page, size));
    }
}
