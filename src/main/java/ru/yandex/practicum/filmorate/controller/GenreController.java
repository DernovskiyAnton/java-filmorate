package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDao;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {

    private final GenreDao genreDao;

    @GetMapping
    public Collection<Genre> findAll() {
        return genreDao.findAll();
    }

    @GetMapping("/{id}")
    public Genre findById(@PathVariable Integer id) {
        return genreDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден."));
    }
}