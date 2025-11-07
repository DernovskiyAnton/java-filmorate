package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDao {

    private final JdbcTemplate jdbcTemplate;

    public Collection<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("genre_id"), rs.getString("name")));
    }

    public Optional<Genre> findById(Integer id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new Genre(rs.getInt("genre_id"), rs.getString("name")), id)
                .stream()
                .findFirst();
    }
}