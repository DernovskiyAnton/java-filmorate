package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private InMemoryFilmStorage filmStorage;

    @Mock
    private InMemoryUserStorage userStorage;

    @InjectMocks
    private FilmService filmService;

    private Film film;
    private User user;

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setLikes(new HashSet<>());

        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void add_shouldAddFilmSuccessfully() {

        when(filmStorage.getNextId()).thenReturn(1L);

        Film result = filmService.add(film);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(filmStorage, times(1)).addFilm(film);
    }

    @Test
    void add_shouldThrowValidationException_whenReleaseDateIsBeforeMinDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmService.add(film)
        );
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
        verify(filmStorage, never()).addFilm(any());
    }

    @Test
    void add_shouldAcceptMinReleaseDate() {

        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        when(filmStorage.getNextId()).thenReturn(1L);

        Film result = filmService.add(film);

        assertNotNull(result);
        verify(filmStorage, times(1)).addFilm(film);
    }

    @Test
    void update_shouldUpdateFilmSuccessfully() {

        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));

        Film result = filmService.update(film);

        assertNotNull(result);
        assertEquals(film.getId(), result.getId());
        verify(filmStorage, times(1)).addFilm(film);
    }

    @Test
    void update_shouldThrowNotFoundException_whenFilmNotExists() {

        when(filmStorage.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmService.update(film)
        );
        assertEquals("Фильм с id = 1 не найден.", exception.getMessage());
    }

    @Test
    void update_shouldThrowValidationException_whenReleaseDateIsInvalid() {

        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmService.update(film)
        );
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void findAll_shouldReturnAllFilms() {

        Film film2 = new Film();
        film2.setId(2L);
        film2.setName("Film 2");

        List<Film> films = Arrays.asList(film, film2);
        when(filmStorage.findAll()).thenReturn(films);

        Collection<Film> result = filmService.findAll();

        assertEquals(2, result.size());
        verify(filmStorage, times(1)).findAll();
    }

    @Test
    void like_shouldAddLikeSuccessfully() {

        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));

        Film result = filmService.like(1L, 1L);

        assertTrue(result.getLikes().contains(1L));
        assertEquals(1, result.getLikes().size());
    }

    @Test
    void like_shouldThrowNotFoundException_whenFilmNotExists() {

        when(filmStorage.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmService.like(1L, 1L)
        );
        assertEquals("Фильм с id = 1 не найден.", exception.getMessage());
    }

    @Test
    void like_shouldThrowNotFoundException_whenUserNotExists() {

        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(userStorage.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmService.like(1L, 1L)
        );
        assertEquals("Пользователь с id = 1 не найден.", exception.getMessage());
    }

    @Test
    void deleteLike_shouldRemoveLikeSuccessfully() {

        film.getLikes().add(1L);
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));

        Film result = filmService.deleteLike(1L, 1L);

        assertFalse(result.getLikes().contains(1L));
        assertEquals(0, result.getLikes().size());
    }

    @Test
    void deleteLike_shouldThrowNotFoundException_whenFilmNotExists() {

        when(filmStorage.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmService.deleteLike(1L, 1L)
        );
        assertEquals("Фильм с id = 1 не найден.", exception.getMessage());
    }

    @Test
    void deleteLike_shouldThrowNotFoundException_whenUserNotExists() {

        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(userStorage.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmService.deleteLike(1L, 1L)
        );
        assertEquals("Пользователь с id = 1 не найден.", exception.getMessage());
    }

    @Test
    void getPopularFilms_shouldReturnFilmsSortedByLikes() {

        Film film1 = new Film();
        film1.setId(1L);
        film1.setName("Film 1");
        film1.setLikes(new HashSet<>(Arrays.asList(1L, 2L, 3L))); // 3 лайка

        Film film2 = new Film();
        film2.setId(2L);
        film2.setName("Film 2");
        film2.setLikes(new HashSet<>(Arrays.asList(1L, 2L))); // 2 лайка

        Film film3 = new Film();
        film3.setId(3L);
        film3.setName("Film 3");
        film3.setLikes(new HashSet<>(Collections.singletonList(1L))); // 1 лайк

        when(filmStorage.findAll()).thenReturn(Arrays.asList(film2, film3, film1));

        Collection<Film> result = filmService.getPopularFilms(2);

        assertEquals(2, result.size());
        List<Film> resultList = new ArrayList<>(result);
        assertEquals(film1.getId(), resultList.get(0).getId()); // Самый популярный
        assertEquals(film2.getId(), resultList.get(1).getId()); // Второй по популярности
    }

    @Test
    void getPopularFilms_shouldReturnAllFilms_whenCountIsGreaterThanFilmsCount() {

        Film film1 = new Film();
        film1.setId(1L);
        film1.setLikes(new HashSet<>());

        Film film2 = new Film();
        film2.setId(2L);
        film2.setLikes(new HashSet<>());

        when(filmStorage.findAll()).thenReturn(Arrays.asList(film1, film2));

        Collection<Film> result = filmService.getPopularFilms(10);

        assertEquals(2, result.size());
    }

    @Test
    void getPopularFilms_shouldReturnEmptyList_whenNoFilms() {

        when(filmStorage.findAll()).thenReturn(Collections.emptyList());

        Collection<Film> result = filmService.getPopularFilms(5);

        assertTrue(result.isEmpty());
    }
}