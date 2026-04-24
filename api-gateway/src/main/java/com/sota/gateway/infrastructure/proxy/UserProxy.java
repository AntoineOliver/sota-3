package com.sota.gateway.infrastructure.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserProxy extends BaseHttpProxy {

    public UserProxy(RestTemplate restTemplate,
                     @Value("${services.user.base-url}") String baseUrl) {
        super(restTemplate, baseUrl);
    }
}
