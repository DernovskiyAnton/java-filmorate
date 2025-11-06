package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final InMemoryFilmStorage inMemoryFilmStorage;
    private final InMemoryUserStorage inMemoryUserStorage;

    public Film add(Film film) {
        log.info("Добавление нового фильма: {}", film.getName());
        validReleaseDate(film);
        film.setId(inMemoryFilmStorage.getNextId());
        inMemoryFilmStorage.addFilm(film);
        log.debug("Фильм успешно добавлен");
        return film;
    }

    public Film update(Film film) {
        log.info("Обновление фильма: {}", film.getName());
        validReleaseDate(film);

        if (inMemoryFilmStorage.findById(film.getId()).isPresent()) {
            inMemoryFilmStorage.addFilm(film);
            log.debug("Фильм успешно обновлен");
            return film;
        }

        log.warn("Фильм с id = {} не найден", film.getId());
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден.");
    }

    public Collection<Film> findAll() {
        Collection<Film> films = inMemoryFilmStorage.findAll();
        return films;
    }

    public Film like(Long filmId, Long userId) {
        Film film = inMemoryFilmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.warn("Фильм с id = {} не найден", filmId);
                    return new NotFoundException("Фильм с id = " + filmId + " не найден.");
                });

        User user = inMemoryUserStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id = {} не найден", userId);
                    return new NotFoundException("Пользователь с id = " + userId + " не найден.");
                });

        film.getLikes().add(userId);

        return film;
    }

    public Film deleteLike(Long filmId, Long userId) {
        Film film = inMemoryFilmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.warn("Фильм с id = {} не найден", filmId);
                    return new NotFoundException("Фильм с id = " + filmId + " не найден.");
                });

        User user = inMemoryUserStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id = {} не найден", userId);
                    return new NotFoundException("Пользователь с id = " + userId + " не найден.");
                });

        film.getLikes().remove(userId);
        log.info("Пользователь с id = {} удалил лайк у фильма с id = {}", userId, filmId);

        return film;
    }

    public Collection<Film> getPopularFilms(int count) {
        log.info("Получение {} самых популярных фильмов", count);

        Collection<Film> popularFilms = inMemoryFilmStorage.findAll().stream()
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