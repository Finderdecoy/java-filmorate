package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Component("UserMemStorage")
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User addUser(User newUser) {
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        if (newUser.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть больше текущей даты");
        }
        logAllFields(newUser);
        Long id = idCreate();
        newUser.setId(id);
        users.put(id, newUser);
        return newUser;
    }

    @Override
    public User editingUser(User editUser) {
        Long userId = editUser.getId();
        if (userId == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(userId)) {
            boolean isChanged = false;
            User oldUser = users.get(userId);
            if (editUser.getName() != null || !editUser.getName().isBlank()) {
                oldUser.setName(editUser.getName());
                isChanged = true;
            }
            if (editUser.getBirthday() != null && !editUser.getBirthday().isAfter(LocalDate.now())) {
                oldUser.setBirthday(editUser.getBirthday());
                isChanged = true;
            }
            if (editUser.getEmail() != null && !editUser.getEmail().isBlank()) {
                oldUser.setEmail(editUser.getEmail());
                isChanged = true;
            }
            if (!isChanged) {
                logAllFields(editUser);
            }
            users.put(userId, oldUser);
            return editUser;
        }
        throw new NotFoundException("Пользователь id = " + userId + " не найден.");
    }

    private long idCreate() {
        if (users.isEmpty()) {
            return 1L;
        }
        return Collections.max(users.keySet()) + 1;
    }

    private void logAllFields(User user) {
        Long id = user.getId();
        String email = user.getEmail();
        String login = user.getLogin();
        String name = user.getName();
        LocalDate birthday = user.getBirthday();

        log.info("Id User: {} \n Имайл: {} \n Логин: {} \n Имя: {} \n День рождения: {}", id, email, login, name, birthday);
    }

    public Optional<User> getByID(Long id) {
        return Optional.ofNullable(users.get(id));
    }
}
