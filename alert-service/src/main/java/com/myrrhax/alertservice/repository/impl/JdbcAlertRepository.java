package com.myrrhax.alertservice.repository.impl;

import com.myrrhax.alertservice.entity.Alert;
import com.myrrhax.alertservice.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcAlertRepository implements AlertRepository {
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Optional<Alert> findById(Long id) {
        final String sql = """
                SELECT id, user_id, sent, created_at
                FROM alert
                WHERE id = :id
                """;

        try {
            Alert alert = jdbc.queryForObject(sql,
                    new MapSqlParameterSource("id", id),
                    new AlertRowMapper());
            return Optional.of(
                    alert
            );
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Alert save(Alert alert) {
        if (alert.getId() == null || alert.getId() == 0) {
            return insert(alert);
        }

        return update(alert);
    }

    private Alert update(Alert alert) {
        final String sql = """
                UPDATE alert
                SET user_id = :userId, sent = :sent, created_at = :createdAt
                WHERE id = :id
                """;
        jdbc.update(sql, new MapSqlParameterSource(
                Map.of(
                        "id", alert.getId(),
                        "userId", alert.getUserId(),
                        "sent", alert.isSent(),
                        "created_at", alert.getCreatedAt()
                )
        ));

        return alert;
    }

    private Alert insert(Alert alert) {
        final String sql = """
                INSERT INTO alert (user_id, sent, created_at)
                VALUES (:userId,:sent,:createdAt)
                """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                sql,
                new MapSqlParameterSource(
                        Map.of(
                                "userId", alert.getUserId(),
                                "sent", alert.isSent(),
                                // ToDo set user timezone
                                "createdAt", OffsetDateTime.now(ZoneId.of("Europe/Moscow"))
                        )
                ),
                keyHolder,
                new String[] {"id"}
        );
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        alert.setId(id);

        return alert;
    }

    public static class AlertRowMapper implements RowMapper<Alert> {
        @Override
        public Alert mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Alert(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getObject("created_at", OffsetDateTime.class),
                    rs.getBoolean("sent")
            );
        }
    }
}
