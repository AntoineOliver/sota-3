package user_service.infrastructure.controller;

import user_service.application.service.UserService;
import user_service.application.dto.*;
import user_service.domain.model.*;

import user_service.application.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -----------------------------
    // USER MANAGEMENT
    // -----------------------------
    @PostMapping("/register")
    public UserResponseDTO registerUser(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String password) {
        try {
            User user = userService.registerUser(name, new Email(email), new Password(password));
            return new UserResponseDTO(user.getId().value(), user.getEmail().value());
        } catch (EmailAlreadyExistException | UserAlreadyRegisterException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{userId}/info")
    public UserInfo getUserInfo(@PathVariable String userId) {
        try {
            return userService.getUserInfo(new UserId(userId));
        } catch (RuntimeException | UserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PutMapping("/password")
    public ResponseEntity<PasswordChangedResponseDTO> changePassword(
            @RequestBody ChangePasswordRequestDTO dto
    ) throws UserNotFoundException, InvalidPasswordException {
        userService.changePassword(
                UserId.is(dto.userId()),
                Password.is(dto.oldPassword()),
                Password.is(dto.newPassword())
        );

        return ResponseEntity.ok(
                new PasswordChangedResponseDTO(
                        dto.userId(),
                        "Password successfully changed"
                )
        );
    }


    @PutMapping("/email")
    public ResponseEntity<EmailUpdatedResponseDTO> updateEmail(
            @RequestBody UpdateEmailRequestDTO dto
    ) throws UserNotFoundException, InvalidPasswordException, EmailAlreadyExistException {
        userService.updateEmail(
                UserId.is(dto.name()),
                Password.is(dto.password()),
                Email.is(dto.email())
        );

        return ResponseEntity.ok(
                new EmailUpdatedResponseDTO(
                        dto.name(),
                        dto.email(),
                        "Email successfully updated"
                )
        );
    }


    // -----------------------------
    // DELIVERY OPERATIONS (DTO ONLY)
    // -----------------------------
    @PostMapping("delivery/create")
    public String createDelivery(@RequestBody DeliveryRequestDTO request) {
        try {
            return userService.createDelivery(new UserId(request.userId()), request);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{userId}/delivery/{deliveryId}/status")
    public DeliveryStatusDTO getDeliveryStatus(@PathVariable String userId,
                                               @PathVariable String deliveryId) {
        try {
            return userService.getDeliveryStatus(new UserId(userId), deliveryId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{userId}/delivery/{deliveryId}/eta")
    public RemainingDurationDTO getRemainingDuration(@PathVariable String userId,
                                                     @PathVariable String deliveryId) {
        try {
            return userService.getRemainingDuration(new UserId(userId), deliveryId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }
}
