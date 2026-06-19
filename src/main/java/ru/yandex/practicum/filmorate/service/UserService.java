package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("UsersDb") UserStorage userStorage, JdbcTemplate jdbc) {
        this.userStorage = userStorage;
    }

    public void addToFriendList(Long firstId, Long secondId) {
        checkUsers(firstId);
        checkUsers(secondId);
        userStorage.addFriend(firstId, secondId);
        var friends = userStorage.getUserFriend(secondId).keySet();
        if (friends.contains(firstId)) {
            userStorage.setStatusFriend(firstId, secondId, true);
            userStorage.setStatusFriend(secondId, firstId, true);
        }
    }

    public Collection<User> getFriendList(Long id) {
        checkUsers(id);
        Set<Long> friendsIds = userStorage.getUserFriend(id).keySet();
        if (friendsIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getUsers().stream().filter(user -> friendsIds.contains(user.getId())).toList();
    }

    public void deleteFromFriendList(Long firstId, Long secondId) {
        checkUsers(firstId);
        checkUsers(secondId);
        userStorage.deleteFriend(firstId, secondId);
        userStorage.setStatusFriend(secondId, firstId, false);
    }

    //Сделал логику в сервисе, хотя руки чесались сделать все 1 sql запросом в Storage.
    public List<User> getCommonFriends(Long firstId, Long secondId) {
        Set<Long> firstFriends = userStorage.getUserFriend(firstId).keySet();
        Set<Long> secondFriends = userStorage.getUserFriend(secondId).keySet();
        List<User> users = userStorage.getUsers().stream().toList();
        List<Long> commonsFriend = firstFriends.stream().filter(secondFriends::contains).toList();
        if (commonsFriend.isEmpty()) throw new NotFoundException("Нет общих друзей");
        return users.stream().filter(user -> commonsFriend.contains(user.getId())).toList();
    }

    public User findUserByID(Long id) {
        Optional<User> user = userStorage.getByID(id);
        if (user.isPresent()) {
            return user.get();
        }
        throw new NotFoundException("Пользователь с таким id " + id + " не найден");
    }

    public void checkUsers(Long id) {
        userStorage.checkUsers(id);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User editingUser(User user) {
        return userStorage.editingUser(user);
    }


}
