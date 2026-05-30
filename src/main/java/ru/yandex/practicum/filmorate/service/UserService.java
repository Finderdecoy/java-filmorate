package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage userStorage;


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
        if (getAllUsers().containsKey(id)) {
            return getAllUsers().get(id);
        }
        throw new NotFoundException("Пользователь с таким id " + id + " не найден");
    }

    public HashMap<Long, User> getAllUsers() {
        return userStorage.getAllUsers();
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
