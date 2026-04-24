package com.sota.gateway.infrastructure.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentProxy extends BaseHttpProxy {

    public PaymentProxy(RestTemplate restTemplate,
                        @Value("${services.payment.base-url}") String baseUrl) {
        super(restTemplate, baseUrl);
    }
}
