package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());

        // Сохраняем жанры фильма
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveFilmGenres(film.getId(), film.getGenres());
        }

        log.info("Создан фильм с id: {}", film.getId());
    }

    public void updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        // Удаляем старые жанры и добавляем новые
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveFilmGenres(film.getId(), film.getGenres());
        }

        log.info("Обновлен фильм с id: {}", film.getId());
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.*, m.name as mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_rating m ON f.rating_id = m.rating_id " +
                "WHERE f.film_id = ?";

        return jdbcTemplate.query(sql, filmRowMapper(), id)
                .stream()
                .findFirst()
                .map(film -> {
                    film.setGenres(loadFilmGenres(id));
                    film.setLikes(loadFilmLikes(id));
                    return film;
                });
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, m.name as mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_rating m ON f.rating_id = m.rating_id";

        Collection<Film> films = jdbcTemplate.query(sql, filmRowMapper());

        // Загружаем жанры и лайки для каждого фильма
        for (Film film : films) {
            film.setGenres(loadFilmGenres(film.getId()));
            film.setLikes(loadFilmLikes(film.getId()));
        }

        return films;
    }

    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = genres.stream()
                .map(genre -> new Object[]{filmId, genre.getId()})
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private LinkedHashSet<Genre> loadFilmGenres(Long filmId) {
        String sql = "SELECT g.genre_id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id";

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("genre_id"), rs.getString("name")), filmId);

        return new LinkedHashSet<>(genres);
    }

    private HashSet<Long> loadFilmLikes(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), filmId));
    }

    private RowMapper<Film> filmRowMapper() {
        return (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            // MPA рейтинг
            int ratingId = rs.getInt("rating_id");
            if (!rs.wasNull()) {
                Mpa mpa = new Mpa();
                mpa.setId(ratingId);
                mpa.setName(rs.getString("mpa_name"));
                film.setMpa(mpa);
            }

            return film;
        };
    }
}