package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component("UsersDb")

public class UserDbStorage implements UserStorage {
    private static final String QUERY_FOR_LIST_USERS = "SELECT * FROM users;";
    private static final String QUERY_FOR_FRIEND_LIST = "SELECT * FROM FRIENDSHIP;";
    private static final String QUERY_FOR_CREATE_USER = "INSERT INTO users(email,login,name,birthday) VALUES(?,?,?,?);";
    private static final String QUERY_FOR_UPDATE_USER = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?;";
    private static final String QUERY_USER_BY_ID = "SELECT * FROM users WHERE id = ? ;";
    private final JdbcTemplate jdbc;
    private HashMap<Long, HashMap<Long, Boolean>> friendList = new HashMap<>();

    @Override
    public Collection<User> getUsers() {
        List<User> users = jdbc.query(QUERY_FOR_LIST_USERS, new UserMapper());
        friendList = getFrendList();
        for (User user : users) {
            user.setFriendList(friendList.get(user.getId()));
        }
        return users;
    }

    @Override
    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть больше текущей даты");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(QUERY_FOR_CREATE_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKey().longValue());
        }
        friendShipListConcatenation(user);
        return user;
    }

    @Override
    public User editingUser(User editUser) {
        Long userId = editUser.getId();
        if (userId == null) {
            throw new ValidationException("Id должен быть указан");
        }
        Optional<User> user = getByID(editUser.getId());
        if (user.isPresent()) {
            User oldUser = user.get();
            if (editUser.getName() != null || !editUser.getName().isBlank()) {
                oldUser.setName(editUser.getName());
            }
            if (editUser.getBirthday() != null && !editUser.getBirthday().isAfter(LocalDate.now())) {
                oldUser.setBirthday(editUser.getBirthday());
            }
            if (editUser.getEmail() != null && !editUser.getEmail().isBlank()) {
                oldUser.setEmail(editUser.getEmail());
            }
            jdbc.update(QUERY_FOR_UPDATE_USER,
                    oldUser.getEmail(),
                    oldUser.getLogin(),
                    oldUser.getName(),
                    oldUser.getBirthday(),
                    oldUser.getId());
            return editUser;
        }
        throw new NotFoundException("Пользователь id = " + userId + " не найден.");
    }

    @Override
    public Optional<User> getByID(Long id) {
        return jdbc.query(QUERY_USER_BY_ID, new UserMapper(), id).stream().map(this::friendShipListConcatenation).findFirst();
    }

    private HashMap<Long, HashMap<Long, Boolean>> getFrendList() {
        return jdbc.query(QUERY_FOR_FRIEND_LIST, rs -> {
            HashMap<Long, HashMap<Long, Boolean>> userAndHisFriends = new HashMap<>();
            while (rs.next()) {
                userAndHisFriends.computeIfAbsent(rs.getLong("user_id"), k -> new HashMap<>())
                        .put(rs.getLong("friend_id"), rs.getBoolean("status"));
            }
            return userAndHisFriends;
        });
    }

    private User friendShipListConcatenation(User user) {
        user.setFriendList(getFrendList().get(user.getId()));
        return user;
    }
}
