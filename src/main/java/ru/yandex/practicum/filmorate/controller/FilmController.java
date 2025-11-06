package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        Film result = filmService.add(film);
        return result;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        Film result = filmService.update(newFilm);
        return result;
    }

    @GetMapping
    public Collection<Film> findAll() {
        Collection<Film> result = filmService.findAll();
        return result;
    }

    @PutMapping("/{id}/like/{userId}")
    public Film like(@PathVariable Long id, @PathVariable Long userId) {
        Film result = filmService.like(id, userId);
        return result;
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        Film result = filmService.deleteLike(id, userId);
        return result;
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        Collection<Film> result = filmService.getPopularFilms(count);
        return result;
    }

}