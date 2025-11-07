package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmDbStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    public FilmService(@Qualifier("filmDbStorage") FilmDbStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       JdbcTemplate jdbcTemplate) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film add(Film film) {
        log.info("Добавление нового фильма: {}", film.getName());
        validReleaseDate(film);
        filmStorage.addFilm(film);
        log.debug("Фильм успешно добавлен");
        return film;
    }

    public Film update(Film film) {
        log.info("Обновление фильма: {}", film.getName());
        validReleaseDate(film);

        if (filmStorage.findById(film.getId()).isPresent()) {
            filmStorage.updateFilm(film);
            log.debug("Фильм успешно обновлен");
            return filmStorage.findById(film.getId()).get();
        }

        log.warn("Фильм с id = {} не найден", film.getId());
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден.");
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден."));
    }

    public Film like(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.warn("Фильм с id = {} не найден", filmId);
                    return new NotFoundException("Фильм с id = " + filmId + " не найден.");
                });

        User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id = {} не найден", userId);
                    return new NotFoundException("Пользователь с id = " + userId + " не найден.");
                });

        String sql = "MERGE INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);

        return filmStorage.findById(filmId).get();
    }

    public Film deleteLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.warn("Фильм с id = {} не найден", filmId);
                    return new NotFoundException("Фильм с id = " + filmId + " не найден.");
                });

        User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id = {} не найден", userId);
                    return new NotFoundException("Пользователь с id = " + userId + " не найден.");
                });

        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);

        log.info("Пользователь с id = {} удалил лайк у фильма с id = {}", userId, filmId);

        return filmStorage.findById(filmId).get();
    }

    public Collection<Film> getPopularFilms(int count) {
        log.info("Получение {} самых популярных фильмов", count);

        Collection<Film> popularFilms = filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());

        log.debug("Возвращено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    private void validReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Ошибка в дате релиза по фильму с id = {}", film.getId());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}