package user_service.infrastructure.repository;

import user_service.application.repository.UserRepository;
import user_service.application.exception.UserNotFoundException;
import user_service.domain.model.*;

import java.util.*;

public class InMemoryUserRepository implements UserRepository {

    private final Map<UserId, User> usersById = new HashMap<>();
    private final Map<Email, User> usersByEmail = new HashMap<>();

    @Override
    public void addUser(User user) {
        usersById.put(user.getId(), user);
        usersByEmail.put(user.getEmail(), user);
    }

    @Override
    public void updateUser(User user) {
        usersById.put(user.getId(), user);
        usersByEmail.put(user.getEmail(), user);
    }

    @Override
    public boolean isPresent(UserId userName) {
        return usersById.containsKey(userName);
    }

    @Override
    public boolean isPresent(Email email) {
        return usersByEmail.containsKey(email);
    }

    @Override
    public User findById(UserId userName) throws UserNotFoundException {
        User user = usersById.get(userName);
        if (user == null) throw new UserNotFoundException(userName);
        return user;
    }

    @Override
    public User findByEmail(Email email) throws UserNotFoundException {
        User user = usersByEmail.get(email);
        if (user == null) throw new UserNotFoundException(email);
        return user;
    }
}
