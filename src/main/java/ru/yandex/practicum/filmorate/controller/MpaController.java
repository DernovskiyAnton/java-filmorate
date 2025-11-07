package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDao;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {

    private final MpaDao mpaDao;

    @GetMapping
    public Collection<Mpa> findAll() {
        return mpaDao.findAll();
    }

    @GetMapping("/{id}")
    public Mpa findById(@PathVariable Integer id) {
        return mpaDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + id + " не найден."));
    }
}