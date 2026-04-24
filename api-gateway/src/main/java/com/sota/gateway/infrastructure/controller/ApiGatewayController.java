package com.sota.gateway.infrastructure.controller;

import com.sota.gateway.infrastructure.proxy.DeliveryProxy;
import com.sota.gateway.infrastructure.proxy.DroneProxy;
import com.sota.gateway.infrastructure.proxy.NotificationProxy;
import com.sota.gateway.infrastructure.proxy.PaymentProxy;
import com.sota.gateway.infrastructure.proxy.UserProxy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiGatewayController {

    private final DeliveryProxy deliveryProxy;
    private final UserProxy userProxy;
    private final PaymentProxy paymentProxy;
    private final DroneProxy droneProxy;
    private final NotificationProxy notificationProxy;

    public ApiGatewayController(DeliveryProxy deliveryProxy,
                                UserProxy userProxy,
                                PaymentProxy paymentProxy,
                                DroneProxy droneProxy,
                                NotificationProxy notificationProxy) {
        this.deliveryProxy = deliveryProxy;
        this.userProxy = userProxy;
        this.paymentProxy = paymentProxy;
        this.droneProxy = droneProxy;
        this.notificationProxy = notificationProxy;
    }

    @RequestMapping({"/deliveries", "/deliveries/**"})
    public ResponseEntity<byte[]> forwardDeliveries(HttpServletRequest request,
                                                    @RequestBody(required = false) byte[] body) {
        return deliveryProxy.forward(resolveMethod(request), downstreamPath(request, "/api/deliveries"), request, body);
    }

    @RequestMapping({"/users", "/users/**"})
    public ResponseEntity<byte[]> forwardUsers(HttpServletRequest request,
                                               @RequestBody(required = false) byte[] body) {
        return userProxy.forward(resolveMethod(request), downstreamPath(request, "/api/users"), request, body);
    }

    @RequestMapping({"/payments", "/payments/**"})
    public ResponseEntity<byte[]> forwardPayments(HttpServletRequest request,
                                                  @RequestBody(required = false) byte[] body) {
        return paymentProxy.forward(resolveMethod(request), downstreamPath(request, "/api/payments"), request, body);
    }

    @RequestMapping({"/drones", "/drones/**"})
    public ResponseEntity<byte[]> forwardDrones(HttpServletRequest request,
                                                @RequestBody(required = false) byte[] body) {
        return droneProxy.forward(resolveMethod(request), downstreamPath(request, "/api/drones"), request, body);
    }

    @RequestMapping({"/notifications", "/notifications/**"})
    public ResponseEntity<byte[]> forwardNotifications(HttpServletRequest request,
                                                       @RequestBody(required = false) byte[] body) {
        return notificationProxy.forward(resolveMethod(request), downstreamPath(request, "/api/notifications"), request, body);
    }

    private HttpMethod resolveMethod(HttpServletRequest request) {
        return HttpMethod.valueOf(request.getMethod());
    }

    private String downstreamPath(HttpServletRequest request, String apiPrefix) {
        String requestUri = request.getRequestURI();
        String path = requestUri.substring(apiPrefix.length());
        return path.isBlank() ? "" : path;
    }
}
