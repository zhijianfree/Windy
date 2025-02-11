package com.zj.service.schedule;

import com.zj.common.enums.ApiType;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.entity.bo.service.ServiceApiBO;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.domain.repository.service.IServiceApiRepository;
import com.zj.service.service.MicroserviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ServiceStaticSchedule {

    private final IMicroServiceRepository microServiceRepository;

    private final IServiceApiRepository serviceApiRepository;

    private final MicroserviceService microserviceService;

    public ServiceStaticSchedule(IMicroServiceRepository microServiceRepository,
                                 IServiceApiRepository serviceApiRepository, MicroserviceService microserviceService) {
        this.microServiceRepository = microServiceRepository;
        this.serviceApiRepository = serviceApiRepository;
        this.microserviceService = microserviceService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void loadApi() {
        List<MicroserviceBO> services = microServiceRepository.getAllServices();
        services.forEach(this::calculateServiceStatics);
    }

    private void calculateServiceStatics(MicroserviceBO service) {
        try {
            List<ServiceApiBO> serviceApiList = serviceApiRepository.getApiByService(service.getServiceId()).stream()
                    .filter(serviceApi -> Objects.equals(serviceApi.getApiType(), ApiType.API.getType()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(serviceApiList)) {
                service.setApiCoverage(0);
                boolean updateResult = microServiceRepository.updateService(service);
                log.info("update service={} 0 coverage result = {}", service.getServiceName(), updateResult);
                return;
            }
            Map<Boolean, List<ServiceApiBO>> serviceApiPartMap =
                    microserviceService.getServiceApiPartMap(service.getServiceId(), serviceApiList);
            List<ServiceApiBO> apiInFeatureList = serviceApiPartMap.get(true);
            double result = Math.round((double) apiInFeatureList.size() / serviceApiList.size() * 100 * 100) / 100.0;
            service.setApiCoverage((int) result);
            boolean updateResult = microServiceRepository.updateService(service);
            log.info("update  service={} coverage result = {} coverage={}", service.getServiceName(), updateResult,
                    result);
        } catch (Exception e) {
            log.info("count service coverage error", e);
        }
    }
}
