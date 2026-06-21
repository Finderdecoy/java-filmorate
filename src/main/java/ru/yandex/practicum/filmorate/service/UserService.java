package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
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
        Set<Long> friendsID = userStorage.getFriendsID(secondId).keySet();
        if (friendsID.contains(firstId)) {
            userStorage.setStatusFriend(firstId, secondId, true);
            userStorage.setStatusFriend(secondId, firstId, true);
        }
    }

    public Collection<User> getFriendList(Long id) {
        checkUsers(id);
        return userStorage.getFriendListUser(id);
    }

    public void deleteFromFriendList(Long firstId, Long secondId) {
        checkUsers(firstId);
        checkUsers(secondId);
        userStorage.deleteFriend(firstId, secondId);
        userStorage.setStatusFriend(secondId, firstId, false);
        log.info("Пользователь-id {} удалил из друзей пользователя-id{}", firstId, secondId);
    }

    public List<User> getCommonFriends(Long firstId, Long secondId) {
        List<User> commonFriends = userStorage.getCommonFriends(firstId, secondId);
        log.info("Вернул общих друзей пользователей {} и {} . Список общих {}", firstId, secondId, commonFriends);
        return commonFriends;
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
