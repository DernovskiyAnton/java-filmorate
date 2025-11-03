package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private InMemoryUserStorage userStorage;

    @InjectMocks
    private UserService userService;

    private User user;
    private User friend;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setLogin("user");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setFriends(new HashSet<>());

        friend = new User();
        friend.setId(2L);
        friend.setEmail("friend@test.com");
        friend.setLogin("friend");
        friend.setName("Friend Name");
        friend.setBirthday(LocalDate.of(1991, 1, 1));
        friend.setFriends(new HashSet<>());
    }

    @Test
    void create_shouldCreateUserSuccessfully() {
        when(userStorage.create(user)).thenReturn(user);

        User result = userService.create(user);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        verify(userStorage, times(1)).create(user);
    }

    @Test
    void create_shouldSetLoginAsName_whenNameIsNull() {
        user.setName(null);
        when(userStorage.create(user)).thenReturn(user);

        User result = userService.create(user);

        assertEquals(user.getLogin(), result.getName());
        verify(userStorage, times(1)).create(user);
    }

    @Test
    void create_shouldSetLoginAsName_whenNameIsBlank() {
        user.setName("   ");
        when(userStorage.create(user)).thenReturn(user);

        User result = userService.create(user);

        assertEquals(user.getLogin(), result.getName());
        verify(userStorage, times(1)).create(user);
    }

    @Test
    void create_shouldThrowValidationException_whenBirthdayIsInFuture() {
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.create(user)
        );
        assertEquals("Дата рождения не может быть позднее сегодня", exception.getMessage());
        verify(userStorage, never()).create(any());
    }

    @Test
    void create_shouldAcceptBirthdayToday() {
        user.setBirthday(LocalDate.now());
        when(userStorage.create(user)).thenReturn(user);

        User result = userService.create(user);

        assertNotNull(result);
        verify(userStorage, times(1)).create(user);
    }

    @Test
    void update_shouldUpdateUserSuccessfully() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.update(user)).thenReturn(user);

        User result = userService.update(user);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        verify(userStorage, times(1)).update(user);
    }

    @Test
    void update_shouldThrowNotFoundException_whenUserNotExists() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.update(user)
        );
        assertEquals("Пользователь с id = 1 не найден.", exception.getMessage());
    }

    @Test
    void update_shouldSetLoginAsName_whenNameIsBlank() {
        user.setName("");
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.update(user)).thenReturn(user);

        User result = userService.update(user);

        assertEquals(user.getLogin(), result.getName());
    }

    @Test
    void update_shouldThrowValidationException_whenBirthdayIsInFuture() {
        user.setBirthday(LocalDate.now().plusYears(1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.update(user)
        );
        assertEquals("Дата рождения не может быть позднее сегодня", exception.getMessage());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        List<User> users = Arrays.asList(user, friend);
        when(userStorage.findAll()).thenReturn(users);

        Collection<User> result = userService.findAll();

        assertEquals(2, result.size());
        verify(userStorage, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyCollection_whenNoUsers() {
        when(userStorage.findAll()).thenReturn(Collections.emptyList());

        Collection<User> result = userService.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldReturnUser_whenUserExists() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void findById_shouldThrowNotFoundException_whenUserNotExists() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.findById(1L)
        );
        assertEquals("Пользователь с id = 1 не найден.", exception.getMessage());
    }

    @Test
    void addFriend_shouldAddFriendSuccessfully() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.of(friend));
        when(userStorage.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.addFriend(1L, 2L);

        assertTrue(user.getFriends().contains(2L));
        assertTrue(friend.getFriends().contains(1L));
        verify(userStorage, times(2)).update(any(User.class));
    }

    @Test
    void addFriend_shouldThrowNotFoundException_whenUserNotExists() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.addFriend(1L, 2L)
        );
        assertEquals("Пользователь с id = 1 не найден.", exception.getMessage());
    }

    @Test
    void addFriend_shouldThrowNotFoundException_whenFriendNotExists() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.addFriend(1L, 2L)
        );
        assertEquals("Пользователь с id = 2 не найден.", exception.getMessage());
    }

    @Test
    void removeFriend_shouldRemoveFriendSuccessfully() {
        user.getFriends().add(2L);
        friend.getFriends().add(1L);

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.of(friend));
        when(userStorage.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.removeFriend(1L, 2L);

        assertFalse(user.getFriends().contains(2L));
        assertFalse(friend.getFriends().contains(1L));
        verify(userStorage, times(2)).update(any(User.class));
    }

    @Test
    void removeFriend_shouldThrowNotFoundException_whenUserNotExists() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.removeFriend(1L, 2L)
        );
        assertEquals("Пользователь с id = 1 не найден.", exception.getMessage());
    }

    @Test
    void removeFriend_shouldThrowNotFoundException_whenFriendNotExists() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.removeFriend(1L, 2L)
        );
        assertEquals("Пользователь с id = 2 не найден.", exception.getMessage());
    }

    @Test
    void removeFriend_shouldNotThrowException_whenFriendshipNotExists() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.of(friend));
        when(userStorage.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> userService.removeFriend(1L, 2L));
    }

    @Test
    void getFriends_shouldReturnListOfFriends() {
        User friend2 = new User();
        friend2.setId(3L);
        friend2.setEmail("friend2@test.com");
        friend2.setLogin("friend2");

        user.getFriends().add(2L);
        user.getFriends().add(3L);

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.of(friend));
        when(userStorage.findById(3L)).thenReturn(Optional.of(friend2));

        List<User> friends = userService.getFriends(1L);

        assertEquals(2, friends.size());
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(2L)));
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(3L)));
    }

    @Test
    void getFriends_shouldReturnEmptyList_whenUserHasNoFriends() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));

        List<User> friends = userService.getFriends(1L);

        assertTrue(friends.isEmpty());
    }

    @Test
    void getFriends_shouldThrowNotFoundException_whenUserNotExists() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getFriends(1L)
        );
        assertEquals("Пользователь с id = 1 не найден.", exception.getMessage());
    }

    @Test
    void getCommonFriends_shouldReturnCommonFriends() {
        User commonFriend1 = new User();
        commonFriend1.setId(3L);

        User commonFriend2 = new User();
        commonFriend2.setId(4L);

        User notCommonFriend = new User();
        notCommonFriend.setId(5L);

        user.getFriends().add(3L);
        user.getFriends().add(4L);
        user.getFriends().add(5L);

        friend.getFriends().add(3L);
        friend.getFriends().add(4L);

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.of(friend));
        when(userStorage.findById(3L)).thenReturn(Optional.of(commonFriend1));
        when(userStorage.findById(4L)).thenReturn(Optional.of(commonFriend2));

        List<User> commonFriends = userService.getCommonFriends(1L, 2L);

        assertEquals(2, commonFriends.size());
        assertTrue(commonFriends.stream().anyMatch(u -> u.getId().equals(3L)));
        assertTrue(commonFriends.stream().anyMatch(u -> u.getId().equals(4L)));
        assertFalse(commonFriends.stream().anyMatch(u -> u.getId().equals(5L)));
    }

    @Test
    void getCommonFriends_shouldReturnEmptyList_whenNoCommonFriends() {
        user.getFriends().add(3L);
        friend.getFriends().add(4L);

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.of(friend));

        List<User> commonFriends = userService.getCommonFriends(1L, 2L);

        assertTrue(commonFriends.isEmpty());
    }

    @Test
    void getCommonFriends_shouldThrowNotFoundException_whenUserNotExists() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getCommonFriends(1L, 2L)
        );
        assertEquals("Пользователь с id = 1 не найден.", exception.getMessage());
    }

    @Test
    void getCommonFriends_shouldThrowNotFoundException_whenOtherUserNotExists() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getCommonFriends(1L, 2L)
        );
        assertEquals("Пользователь с id = 2 не найден.", exception.getMessage());
    }

    @Test
    void getCommonFriends_shouldReturnEmptyList_whenBothUsersHaveNoFriends() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.of(friend));

        List<User> commonFriends = userService.getCommonFriends(1L, 2L);

        assertTrue(commonFriends.isEmpty());
    }
}
