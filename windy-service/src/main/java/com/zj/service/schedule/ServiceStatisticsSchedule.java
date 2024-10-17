package com.zj.service.schedule;

import com.zj.common.enums.InvokerType;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.dto.feature.ExecuteTemplateDto;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.dto.feature.FeatureType;
import com.zj.domain.entity.dto.feature.TestCaseDto;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITestCaseRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.domain.repository.service.IServiceApiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ServiceStatisticsSchedule {

    private final IMicroServiceRepository microServiceRepository;
    private final ITestCaseRepository testCaseRepository;
    private final IServiceApiRepository serviceApiRepository;

    private final IExecutePointRepository executePointRepository;
    private final IExecuteTemplateRepository executeTemplateRepository;
    private final IFeatureRepository featureRepository;

    public ServiceStatisticsSchedule(IMicroServiceRepository microServiceRepository,
                                     ITestCaseRepository testCaseRepository,
                                     IServiceApiRepository serviceApiRepository,
                                     IExecutePointRepository executePointRepository,
                                     IExecuteTemplateRepository executeTemplateRepository,
                                     IFeatureRepository featureRepository) {
        this.microServiceRepository = microServiceRepository;
        this.testCaseRepository = testCaseRepository;
        this.serviceApiRepository = serviceApiRepository;
        this.executePointRepository = executePointRepository;
        this.executeTemplateRepository = executeTemplateRepository;
        this.featureRepository = featureRepository;
    }

    @Scheduled()
    public void runCollect() {
        List<MicroserviceDto> allServices = microServiceRepository.getAllServices();
        allServices.forEach(service -> {
            List<TestCaseDto> serviceCases = testCaseRepository.getServiceCases(service.getServiceId());
            serviceCases.stream().map(testCase -> {
                List<String> caseFeatureIds = featureRepository.getCaseFeatures(testCase.getTestCaseId())
                        .stream().filter(feature -> Objects.equals(feature.getFeatureType(),
                                FeatureType.ITEM.getType()))
                        .map(FeatureInfoDto::getFeatureId).collect(Collectors.toList());
                List<String> executePointTemplateIds = executePointRepository.getPointsByFeatureIds(caseFeatureIds)
                        .stream().map(ExecutePointDto::getTemplateId).distinct().collect(Collectors.toList());
                return executeTemplateRepository.getTemplateByIds(executePointTemplateIds)
                        .stream().filter(template -> Objects.equals(template.getInvokeType(), InvokerType.HTTP.getType()))
                        .map(ExecuteTemplateDto::getService)
                        .collect(Collectors.toList());

            })
        });

    }
}
