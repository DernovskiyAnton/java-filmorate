package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

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

    public boolean allExist(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }

        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id IN (" +
                String.join(",", Collections.nCopies(ids.size(), "?")) + ")";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, ids.toArray());
        return count != null && count == ids.size();
    }
}