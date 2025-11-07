package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MpaDao {
    private final JdbcTemplate jdbcTemplate;

    public Collection<Mpa> findAll() {
        String sql = "SELECT * FROM mpa_rating ORDER BY rating_id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Mpa(rs.getInt("rating_id"), rs.getString("name")));
    }

    public Optional<Mpa> findById(Integer id) {
        String sql = "SELECT * FROM mpa_rating WHERE rating_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new Mpa(rs.getInt("rating_id"), rs.getString("name")), id)
                .stream()
                .findFirst();
    }

    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM mpa_rating WHERE rating_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}