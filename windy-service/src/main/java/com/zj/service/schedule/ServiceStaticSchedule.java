package com.zj.service.schedule;

import com.zj.common.enums.ApiType;
import com.zj.common.enums.InvokerType;
import com.zj.domain.entity.bo.feature.ExecutePointBO;
import com.zj.domain.entity.bo.feature.ExecuteTemplateBO;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.entity.bo.feature.TestCaseBO;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.entity.bo.service.ServiceApiBO;
import com.zj.domain.entity.enums.FeatureType;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITestCaseRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.domain.repository.service.IServiceApiRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ServiceStaticSchedule {

    private final IMicroServiceRepository microServiceRepository;
    private final ITestCaseRepository testCaseRepository;
    private final IFeatureRepository featureRepository;
    private final IExecutePointRepository executePointRepository;
    private final IExecuteTemplateRepository executeTemplateRepository;
    private final IServiceApiRepository serviceApiRepository;

    public ServiceStaticSchedule(IMicroServiceRepository microServiceRepository,
                                 ITestCaseRepository testCaseRepository, IFeatureRepository featureRepository,
                                 IExecutePointRepository executePointRepository,
                                 IExecuteTemplateRepository executeTemplateRepository,
                                 IServiceApiRepository serviceApiRepository) {
        this.microServiceRepository = microServiceRepository;
        this.testCaseRepository = testCaseRepository;
        this.featureRepository = featureRepository;
        this.executePointRepository = executePointRepository;
        this.executeTemplateRepository = executeTemplateRepository;
        this.serviceApiRepository = serviceApiRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void loadApi() {
        List<MicroserviceBO> services = microServiceRepository.getAllServices();
        services.forEach(this::calculateServiceStatics);
    }

    private void calculateServiceStatics(MicroserviceBO service) {
        try {
            List<ExecuteTemplateBO> templates = getServiceAllExecuteTemplate(service);
            if (CollectionUtils.isEmpty(templates)) {
                service.setApiCoverage(0);
                boolean updateResult = microServiceRepository.updateService(service);
                log.info("update  service coverage result = {}", updateResult);
                return;
            }
            List<String> templateApiList =
                    templates.stream().map(ExecuteTemplateBO::getService).collect(Collectors.toList());

            List<ServiceApiBO> serviceApiList = serviceApiRepository.getApiByService(service.getServiceId()).stream()
                    .filter(serviceApi -> Objects.equals(serviceApi.getApiType(), ApiType.API.getType())).collect(Collectors.toList());
            List<ServiceApiBO> apiInFeatureList = serviceApiList.stream()
                    .filter(serviceApi -> templateApiList.stream().anyMatch(templateApi ->
                            templateApi.contains(serviceApi.getResource()))).collect(Collectors.toList());
            double result = Math.round((double) apiInFeatureList.size() / serviceApiList.size() * 100 * 100) / 100.0;
            service.setApiCoverage((int) result);
            boolean updateResult = microServiceRepository.updateService(service);
            log.info("update  service coverage result = {}", updateResult);
        }catch (Exception e){
            log.info("count service coverage error", e);
        }
    }

    private List<ExecuteTemplateBO> getServiceAllExecuteTemplate(MicroserviceBO service) {
        List<String> caseIds =
                testCaseRepository.getServiceCases(service.getServiceId()).stream().map(TestCaseBO::getTestCaseId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(caseIds)) {
            return Collections.emptyList();
        }
        List<String> featureIds = featureRepository.getFeatureByCases(caseIds).stream()
                .filter(feature -> Objects.equals(feature.getFeatureType(), FeatureType.ITEM.getType()))
                .map(FeatureInfoBO::getFeatureId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(featureIds)) {
            return Collections.emptyList();
        }
        List<String> templateIds = executePointRepository.getPointsByFeatureIds(featureIds).stream()
                .map(ExecutePointBO::getTemplateId).distinct().collect(Collectors.toList());
        return executeTemplateRepository.getTemplateByIds(templateIds).stream()
                .filter(executeTemplate -> Objects.equals(executeTemplate.getInvokeType(),
                InvokerType.HTTP.getType())).collect(Collectors.toList());
    }
}
