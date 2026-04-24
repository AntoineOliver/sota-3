package user_service.application.exception;

import user_service.domain.model.Email;
import user_service.domain.model.UserId;

public class UserNotFoundException extends Exception {

    public UserNotFoundException(UserId userName) {
    }

    public UserNotFoundException(Email email) {
    }
}