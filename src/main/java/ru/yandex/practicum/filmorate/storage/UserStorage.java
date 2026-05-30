package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;

@Component
public interface UserStorage {
    public Collection<User> getUsers();

    public User addUser(User user);

    public User editingUser(User user);

    public HashMap<Long, User> getAllUsers();
}
