package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class UserService {
    private UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addToFriendList(Long firstId, Long secondId) {
        User firstFriend = findUserByID(firstId);
        User secondFriend = findUserByID(secondId);

        firstFriend.getFriendList().add(secondId);
        secondFriend.getFriendList().add(firstId);
    }

    public List<User> getFriendList(Long userId) {
        return findUserByID(userId).getFriendList().stream().map(this::findUserByID).toList();
    }

    public void deleteFromFriendList(Long firstId, Long secondId) {
        User firstFriend = findUserByID(firstId);
        User secondFriend = findUserByID(secondId);

        firstFriend.getFriendList().remove(secondId);
        secondFriend.getFriendList().remove(firstId);
    }

    public List<User> getCommonFriends(Long firstId, Long secondId) {
        Set<Long> listFirstFriend = findUserByID(firstId).getFriendList();
        Set<Long> listSecondFriend = findUserByID(secondId).getFriendList();

        return listFirstFriend.stream().filter(listSecondFriend::contains).map(this::findUserByID).toList();
    }

    public User findUserByID(Long id) {
        return userStorage.getUsers().stream()
                .filter(user -> Objects.equals(user.getId(), id))
                .findFirst().orElseThrow(() -> new NotFoundException("Пользователь с таким id " + id + " не найден"));
    }
}
