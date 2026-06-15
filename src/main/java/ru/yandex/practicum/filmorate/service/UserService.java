package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mapper.UserMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final JdbcTemplate jdbc;

    public UserService(@Qualifier("UsersDb") UserStorage userStorage, JdbcTemplate jdbc) {
        this.userStorage = userStorage;
        this.jdbc = jdbc;
    }

    public void addToFriendList(Long firstId, Long secondId) {
        checkUsers(firstId);
        checkUsers(secondId);
        String checkSql = "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, secondId, firstId);
        boolean isFriend = (count != null && count > 0);
        if (isFriend) {
            jdbc.update("UPDATE friendship SET status = true WHERE user_id = ? AND friend_id = ?", secondId, firstId);
            String mergeTrueSql = "MERGE INTO friendship (user_id, friend_id, status) KEY(user_id, friend_id) VALUES (?, ?, true)";
            jdbc.update(mergeTrueSql, firstId, secondId);
        } else {
            String mergeFalseSql = "MERGE INTO friendship (user_id, friend_id, status) KEY(user_id, friend_id) VALUES (?, ?, false)";
            jdbc.update(mergeFalseSql, firstId, secondId);
        }
    }


    public List<User> getFriendList(Long userId) {
        checkUsers(userId);
        List<User> list = jdbc.query("SELECT friend_id FROM friendship WHERE user_id = ?;", rs -> {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                var user = userStorage.getByID(rs.getLong(1));
                user.ifPresent(users::add);
            }
            return users;
        }, userId);
        return list;
    }

    public void deleteFromFriendList(Long firstId, Long secondId) {
        checkUsers(firstId);
        checkUsers(secondId);
        jdbc.update("DELETE FROM FRIENDSHIP WHERE USER_ID = ? AND FRIEND_ID = ?", firstId, secondId);
    }

    public List<User> getCommonFriends(Long firstId, Long secondId) {
        String sql = "SELECT u.id, u.email,u.login,u.name,u.birthday" +
                " FROM users AS u " +
                "JOIN FRIENDSHIP f1 ON u.ID = f1.FRIEND_ID " +
                "JOIN FRIENDSHIP f2 ON u.ID = f2.FRIEND_ID " +
                "WHERE f1.USER_ID = ? AND f2.USER_ID = ?; ";
        return jdbc.query(sql, new UserMapper(), firstId, secondId);
    }

    public User findUserByID(Long id) {
        Optional<User> user = userStorage.getByID(id);
        if (user.isPresent()) {
            return user.get();
        }
        throw new NotFoundException("Пользователь с таким id " + id + " не найден");
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

    public void checkUsers(Long firstId) {
        String sql = "SELECT COUNT(*) FROM USERS WHERE ID = ?";
        Integer countFirst = jdbc.queryForObject(sql, Integer.class, firstId);
        if (countFirst == null || countFirst == 0) {
            throw new NotFoundException("Пользователь с id " + firstId + " не найден в БД");
        }
    }
}
