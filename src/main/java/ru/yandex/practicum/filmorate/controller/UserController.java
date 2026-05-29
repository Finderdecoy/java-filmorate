package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private UserStorage userStorage;
    private UserService userService;

    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User newUser) { //что смог, пустил в валидатор
        return userStorage.addUser(newUser);
    }

    @PutMapping
    public User editingUser(@RequestBody User editUser) {
        return userStorage.editingUser(editUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriends(@PathVariable Long id,
                           @PathVariable Long friendId) {
        userService.addToFriendList(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deletFriends(@PathVariable Long id,
                             @PathVariable Long friendId) {
        userService.deleteFromFriendList(id, friendId);
    }

    @GetMapping("{id}/friends")
    public Collection<User> getFriendList(@PathVariable Long id) {
        return userService.getFriendList(id);
    }

    @GetMapping("{id}/friends/common/{friendId}")
    public Collection<User> getCommonFriends(@PathVariable Long id,
                                             @PathVariable Long friendId) {
        return userService.getCommonFriends(id, friendId);
    }
}
