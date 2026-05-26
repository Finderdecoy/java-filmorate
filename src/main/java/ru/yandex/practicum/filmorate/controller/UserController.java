package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final HashMap<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User newUser) { //что смог, пустил в валидатор
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

    @PutMapping
    public User editingUser(@RequestBody User editUser) {
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
        throw new RuntimeException("Пользователь id = " + userId + " не найден.");
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
}
