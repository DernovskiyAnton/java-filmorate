package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, JdbcTemplate jdbcTemplate) {
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public User create(User user) {
        validateUser(user);
        setNameIfEmpty(user);
        User createdUser = userStorage.create(user);
        log.info("Создан пользователь: {}", createdUser);
        return createdUser;
    }

    public User update(User user) {
        setNameIfEmpty(user);
        validateUser(user);
        if (userStorage.findById(user.getId()).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден.");
        }

        User updatedUser = userStorage.update(user);
        log.info("Обновлен пользователь: {}", updatedUser);
        return updatedUser;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден."));
    }

    public void addFriend(Long userId, Long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        String sql = "MERGE INTO user_friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        String sql = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);

        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        User user = findById(userId);

        String sql = "SELECT u.* FROM users u " +
                "JOIN user_friends uf ON u.user_id = uf.friend_id " +
                "WHERE uf.user_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User friend = new User();
            friend.setId(rs.getLong("user_id"));
            friend.setEmail(rs.getString("email"));
            friend.setLogin(rs.getString("login"));
            friend.setName(rs.getString("name"));
            friend.setBirthday(rs.getDate("birthday").toLocalDate());
            return friend;
        }, userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = findById(userId);
        User otherUser = findById(otherId);

        String sql = "SELECT u.* FROM users u " +
                "WHERE u.user_id IN (" +
                "  SELECT uf1.friend_id FROM user_friends uf1 " +
                "  WHERE uf1.user_id = ? " +
                "  INTERSECT " +
                "  SELECT uf2.friend_id FROM user_friends uf2 " +
                "  WHERE uf2.user_id = ?" +
                ")";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User friend = new User();
            friend.setId(rs.getLong("user_id"));
            friend.setEmail(rs.getString("email"));
            friend.setLogin(rs.getString("login"));
            friend.setName(rs.getString("name"));
            friend.setBirthday(rs.getDate("birthday").toLocalDate());
            return friend;
        }, userId, otherId);
    }

    private void setNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validateUser(User user) {
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть позднее сегодня");
        }
    }
}