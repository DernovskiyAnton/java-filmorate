package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(FilmDbStorage.class)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    public void testAddAndFindFilmById() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        filmStorage.addFilm(film);

        assertThat(film.getId()).isNotNull();

        Optional<Film> filmOptional = filmStorage.findById(film.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f).hasFieldOrPropertyWithValue("name", "Test Film");
                    assertThat(f).hasFieldOrPropertyWithValue("description", "Test Description");
                    assertThat(f.getMpa()).isNotNull();
                    assertThat(f.getMpa().getId()).isEqualTo(1);
                });
    }

    @Test
    public void testAddFilmWithGenres() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(1, "Комедия"));
        genres.add(new Genre(2, "Драма"));
        film.setGenres(genres);

        filmStorage.addFilm(film);

        Optional<Film> filmOptional = filmStorage.findById(film.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getGenres()).hasSize(2);
                    assertThat(f.getGenres()).extracting(Genre::getId).containsExactly(1, 2);
                });
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film();
        film.setName("Original Film");
        film.setDescription("Original Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        filmStorage.addFilm(film);

        film.setName("Updated Film");
        film.setDescription("Updated Description");

        filmStorage.updateFilm(film);

        Optional<Film> updatedFilm = filmStorage.findById(film.getId());

        assertThat(updatedFilm)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f).hasFieldOrPropertyWithValue("name", "Updated Film");
                    assertThat(f).hasFieldOrPropertyWithValue("description", "Updated Description");
                });
    }

    @Test
    public void testFindAllFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(120);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2021, 1, 1));
        film2.setDuration(90);

        filmStorage.addFilm(film1);
        filmStorage.addFilm(film2);

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    public void testFindFilmById_NotFound() {
        Optional<Film> filmOptional = filmStorage.findById(999L);

        assertThat(filmOptional).isEmpty();
    }
}
