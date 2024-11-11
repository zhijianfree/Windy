package com.zj.common.adapter.invoker;

import com.zj.common.adapter.invoker.eureka.EurekaClientInvokerAdapter;
import com.zj.common.adapter.discover.DiscoverService;
import com.zj.common.adapter.invoker.eureka.EurekaMasterInvokerAdapter;
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
