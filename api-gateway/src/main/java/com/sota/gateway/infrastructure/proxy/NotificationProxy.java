package com.sota.gateway.infrastructure.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationProxy extends BaseHttpProxy {

    public NotificationProxy(RestTemplate restTemplate,
                             @Value("${services.notification.base-url}") String baseUrl) {
        super(restTemplate, baseUrl);
    }
}
