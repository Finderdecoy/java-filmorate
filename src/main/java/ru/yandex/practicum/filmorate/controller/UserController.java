package ru.yandex.practicum.filmorate.controller;

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
    public User addUser(@RequestBody User newUser) {
        if (!newUser.getEmail().contains("@")) {
            throw new ValidationException("Не верно заполнено поле Email");
        }
        if (newUser.getLogin().contains(" ") || newUser.getLogin().isBlank()) {
            throw new ValidationException("Логин не должен содержать пробелы или не должен быть пустым");
        }
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
            if (editUser.getName() == null && editUser.getName().isBlank() || editUser.getBirthday().isAfter(LocalDate.now()) || editUser.getEmail().isBlank()) {
                logAllFields(editUser);
                throw new ValidationException("Новое имя, и дата рождения не могут быть пустыми");
            }
            users.put(userId, editUser);
            return editUser;
        }
        throw new RuntimeException("Данные не были изменены, проверьте правильность заполнения");
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
