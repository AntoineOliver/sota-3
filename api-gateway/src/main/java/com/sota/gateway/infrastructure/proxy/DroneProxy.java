package com.sota.gateway.infrastructure.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DroneProxy extends BaseHttpProxy {

    public DroneProxy(RestTemplate restTemplate,
                      @Value("${services.drone.base-url}") String baseUrl) {
        super(restTemplate, baseUrl);
    }
}
