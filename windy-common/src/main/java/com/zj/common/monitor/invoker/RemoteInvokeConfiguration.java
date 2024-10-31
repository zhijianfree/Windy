package com.zj.common.monitor.invoker;

import com.zj.common.monitor.discover.DiscoverService;
import com.zj.common.monitor.invoker.eureka.EurekaMasterInvokerAdapter;
import com.zj.common.monitor.invoker.eureka.EurekaClientInvokerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RemoteInvokeConfiguration {

    @Bean
    public IMasterInvoker getMasterInvoker(RestTemplate restTemplate, DiscoverService discoverService) {
        return new EurekaMasterInvokerAdapter(restTemplate, discoverService);
    }

    @Bean
    public IClientInvoker getClientInvoker(RestTemplate restTemplate, DiscoverService discoverService) {
        return new EurekaClientInvokerAdapter(discoverService, restTemplate);
    }
}
