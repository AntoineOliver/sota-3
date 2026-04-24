package com.sota.gateway.infrastructure.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DeliveryProxy extends BaseHttpProxy {

    public DeliveryProxy(RestTemplate restTemplate,
                         @Value("${services.delivery.base-url}") String baseUrl) {
        super(restTemplate, baseUrl);
    }
}
