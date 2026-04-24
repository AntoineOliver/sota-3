package user_service.application.service;

import user_service.application.dto.*;
import user_service.application.exception.*;
import user_service.application.port.DeliveryPort;
import user_service.application.port.UserEventPublisherPort;
import user_service.application.repository.UserRepository;
import user_service.domain.event.UserEmailUpdatedEvent;
import user_service.domain.event.UserRegisteredEvent;
import user_service.domain.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final DeliveryPort deliveryPort;
    private final UserEventPublisherPort eventPublisher;

    public UserServiceImpl(UserRepository userRepository, DeliveryPort deliveryPort, UserEventPublisherPort eventPublisher) {
        this.userRepository = userRepository;
        this.deliveryPort = deliveryPort;
        this.eventPublisher = eventPublisher;
    }

    // --- User management ---
    @Override
    public User registerUser(String name, Email email, Password password) throws UserAlreadyRegisterException, EmailAlreadyExistException {
        log.info("[USER][REGISTER] registering new user={}", name);

        if (userRepository.isPresent(email)) throw new EmailAlreadyExistException();
        log.info("[USER][REGISTER] user does not exist name={}", name);
        UserId userId;
        int attempts = 0;
        do {
            userId = UserId.fromUsername(name);
            attempts++;
            if (attempts > 10) throw new UserAlreadyRegisterException();
        } while (userRepository.isPresent(userId));
        log.info("[USER][REGISTER] userId created id={}", userId);
        User user = new User(userId, password, email);
        log.info("[USER][REGISTER] user created name={} and id={}", name, user.getId());
        userRepository.addUser(user);
        log.info("[USER][REGISTER] repo updated id={}", user.getId());
        // publish event async
        try {
            UserRegisteredEvent ev = new UserRegisteredEvent(
                    user.getId().value(),
                    name,
                    user.getEmail().value(),
                    Instant.now()
            );
            eventPublisher.publish(ev);
        } catch (Exception e) {
            log.error("[USER] failed to publish event", e);
        }
        log.info("[USER][REGISTER] event published id={}", user.getId());
        return user;
    }

    @Override
    public void changePassword(UserId userId, Password oldPassword, Password newPassword) throws UserNotFoundException, InvalidPasswordException {
        log.info("[USER][PASSWORD] changing password id={}", userId);
        var user = userRepository.findById(userId);
        log.info("[USER][PASSWORD] user found id={}", userId);
        if (!user.getPassword().matches(oldPassword)) throw new InvalidPasswordException();
        user.withPassword(newPassword);
        log.info("[USER][PASSWORD] password changed id={}", userId);
        userRepository.updateUser(user);
        log.info("[USER][PASSWORD] repo updated id={}", userId);
    }

    @Override
    public void updateEmail(UserId userId, Password password, Email newEmail) throws UserNotFoundException, EmailAlreadyExistException, InvalidPasswordException {
        log.info("[USER][EMAIL] changing email id={}", userId);
        var user = userRepository.findById(userId);
        log.info("[USER][EMAIL] user found id={}", userId);
        if (!user.getPassword().matches(password)) throw new InvalidPasswordException();
        if (userRepository.isPresent(newEmail)) throw new EmailAlreadyExistException();

        String oldEmail = user.getEmail().toString();
        user.withEmail(newEmail);
        log.info("[USER][EMAIL] email changed id={}", userId);
        userRepository.updateUser(user);
        log.info("[USER][EMAIL] repo updated id={}", userId);

        try {
            UserEmailUpdatedEvent ev =
                    new UserEmailUpdatedEvent(userId.toString(), oldEmail, newEmail.toString(), Instant.now());

            eventPublisher.publish(ev);
        } catch (Exception e) {
            log.error("[USER] failed to publish event", e);
        }
        log.info("[USER][EMAIL] event published id={}", userId);
    }

    @Override
    public UserInfo getUserInfo(UserId userId) throws UserNotFoundException {
        return userRepository.findById(userId).getUserInfo();
    }

    @Override
    public boolean isValidPassword(UserId userId, Password password) throws UserNotFoundException {
        return userRepository.findById(userId).getPassword().matches(password);
    }

    // --- Delivery related ---
    @Override
    public String createDelivery(UserId userId, DeliveryRequestDTO request) throws Exception {
        log.info("[USER][DELIVERY] creating delivery id={}", userId);
        String deliveryId = deliveryPort.createDelivery(request);
        log.info("[USER][DELIVERY] request sent id={}", userId);
        User user = userRepository.findById(userId);
        log.info("[USER][DELIVERY] user found id={}", userId);
        user.addDeliveryId(deliveryId);
        log.info("[USER][DELIVERY] delivery id={} added to user id={}", deliveryId,user.getId());
        userRepository.updateUser(user);
        log.info("[USER][DELIVERY] repo updated id={}", userId);
        return deliveryId;
    }

    @Override
    public DeliveryStatusDTO getDeliveryStatus(UserId userId, String deliveryId) throws Exception {
        // you might check ownership here: ensure the deliveryId belongs to user
        return deliveryPort.getDeliveryStatus(deliveryId);
    }

    @Override
    public RemainingDurationDTO getRemainingDuration(UserId userId, String deliveryId) throws Exception {
        return deliveryPort.getRemainingDuration(deliveryId);
    }
}
