package user_service.domain.model;

import common.ddd.Aggregate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class User implements Aggregate<UserId> {

    private final UserId userId;
    private Password password;
    private Email email;
    private final List<String> deliveryIds;

    public User(UserId userId, Password password, Email email) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.deliveryIds = new ArrayList<>();
    }

    @Override
    public UserId getId() {
        return userId;
    }

    public Password getPassword() {
        return password;
    }

    public void withPassword(Password newPassword) {
        this.password = newPassword;
    }

    public Email getEmail() {
        return email;
    }

    public void withEmail(Email newEmail) {
        this.email = newEmail;
    }

    public UserInfo getUserInfo() {
        return new UserInfo(userId.toString(), email.toString(), Instant.now().toEpochMilli());
    }

    public void addDeliveryId(String deliveryId) {
        this.deliveryIds.add(deliveryId);
    }
}
