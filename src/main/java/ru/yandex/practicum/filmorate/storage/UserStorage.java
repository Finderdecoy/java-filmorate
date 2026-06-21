package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getUsers();

    User addUser(User user);

    User editingUser(User user);

    Optional<User> getByID(Long id);

    Map<Long, Boolean> getFriendsID(Long id);

    void addFriend(Long idUser, Long idFriend);

    void deleteFriend(Long idUser, Long idFriend);

    void setStatusFriend(Long idUser, Long idFriend, Boolean status);

    void checkUsers(Long id);

    List<User> getCommonFriends(Long firstId, Long secondId);

    Collection<User> getFriendListUser(Long id);
}
