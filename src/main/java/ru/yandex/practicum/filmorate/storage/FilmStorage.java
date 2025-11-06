package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    public void addFilm(Film film);

    public Optional<Film> findById(Long id);

    public Collection<Film> findAll();

}
