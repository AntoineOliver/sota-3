package user_service.application.service;

import common.hexagonal.InBoundPort;
import user_service.application.dto.*;
import user_service.application.exception.*;
import user_service.domain.model.*;

@InBoundPort
public interface UserService {

    // --- User management ---
    User registerUser(String name, Email email, Password password) throws UserAlreadyRegisterException, EmailAlreadyExistException;

    void changePassword(UserId userId, Password oldPassword, Password newPassword) throws UserNotFoundException, InvalidPasswordException;

    void updateEmail(UserId userId, Password password, Email newEmail) throws UserNotFoundException, EmailAlreadyExistException, InvalidPasswordException;

    UserInfo getUserInfo(UserId userId) throws UserNotFoundException;

    boolean isValidPassword(UserId userId, Password password) throws UserNotFoundException;

    // --- Delivery related (synchronous calls via port) ---
    String createDelivery(UserId userId, DeliveryRequestDTO request) throws Exception;

    DeliveryStatusDTO getDeliveryStatus(UserId userId, String deliveryId) throws Exception;

    RemainingDurationDTO getRemainingDuration(UserId userId, String deliveryId) throws Exception;
}
