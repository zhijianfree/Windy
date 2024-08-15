package com.zj.demand.service;

import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.uuid.UniqueIdService;
import com.zj.demand.entity.IterationStatistic;
import com.zj.domain.entity.dto.demand.IterationDTO;
import com.zj.domain.repository.demand.IBugRepository;
import com.zj.domain.repository.demand.IDemandRepository;
import com.zj.domain.repository.demand.IWorkTaskRepository;
import com.zj.domain.repository.demand.IterationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class IterationService {

    private final IDemandRepository demandRepository;
    private final IBugRepository bugRepository;
    private final IWorkTaskRepository workTaskRepository;
    private final IterationRepository iterationRepository;
    private final UniqueIdService uniqueIdService;
    private final IAuthService authService;

    public IterationService(IDemandRepository demandRepository, IBugRepository bugRepository,
                            IWorkTaskRepository workTaskRepository, IterationRepository iterationRepository,
                            UniqueIdService uniqueIdService, IAuthService authService) {
        this.demandRepository = demandRepository;
        this.bugRepository = bugRepository;
        this.workTaskRepository = workTaskRepository;
        this.iterationRepository = iterationRepository;
        this.uniqueIdService = uniqueIdService;
        this.authService = authService;
    }

    public List<IterationDTO> getIterationList() {
        return iterationRepository.getIterationList();
    }

    public IterationDTO createIteration(IterationDTO iterationDTO) {
        iterationDTO.setIterationId(uniqueIdService.getUniqueId());
        iterationDTO.setUserId(authService.getCurrentUserId());
        return iterationRepository.createIteration(iterationDTO);
    }

    public Boolean updateIteration(String iterationId, IterationDTO iterationDTO) {
        IterationDTO iteration = getIteration(iterationId);
        if (Objects.isNull(iteration)) {
            log.info("iteration is not exist={}", iterationId);
            throw new ApiException(ErrorCode.ITERATION_NOT_EXIST);
        }
        return iterationRepository.updateIteration(iterationDTO);
    }

    public boolean deleteIteration(String iterationId) {
        return iterationRepository.deleteIteration(iterationId);
    }

    public IterationDTO getIteration(String iterationId) {
        return iterationRepository.getIteration(iterationId);
    }

    public IterationStatistic getIterationStatistic(String iterationId) {
        Integer demandCount = demandRepository.countIteration(iterationId);
        Integer bugCount = bugRepository.countIteration(iterationId);
        Integer workCount = workTaskRepository.countIteration(iterationId);
        return new IterationStatistic(demandCount, bugCount, workCount, 0);
    }
}
