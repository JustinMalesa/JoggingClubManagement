
package com.example.joggingclubapi.repository;

/**
 *
 * @author justi
 */

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public SessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Retrieve all sessions and their registration counts
    public List<Map<String, Object>> findAll() {

        String sql = "SELECT s.session_id, s.session_name, s.category, "
                + "s.session_date, s.session_time, s.location, "
                + "s.capacity, s.status, "
                + "(SELECT COUNT(*) FROM registrations r "
                + "WHERE r.session_id = s.session_id) "
                + "AS registered_count "
                + "FROM sessions s "
                + "ORDER BY s.session_date, s.session_time";

        return jdbcTemplate.queryForList(sql);
    }

    // Save a new session
    public int save(
            String name,
            String category,
            String date,
            String time,
            String location,
            int capacity) {

        String sql = "INSERT INTO sessions "
                + "(session_name, category, session_date, session_time, "
                + "location, capacity, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.update(
                sql, name, category, date, time,
                location, capacity, "Open"
        );
    }

    // Update an existing session
    public int update(
            String name,
            String category,
            String date,
            String time,
            String location,
            int capacity,
            int id) {

        String sql = "UPDATE sessions SET session_name = ?, "
                + "category = ?, session_date = ?, session_time = ?, "
                + "location = ?, capacity = ? "
                + "WHERE session_id = ?";

        return jdbcTemplate.update(
                sql, name, category, date, time,
                location, capacity, id
        );
    }

    // Count registrations for a session
    public Integer countRegistrations(int id) {

        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM registrations WHERE session_id = ?",
                Integer.class,
                id
        );
    }

    // Delete a session
    public int delete(int id) {

        return jdbcTemplate.update(
                "DELETE FROM sessions WHERE session_id = ?", id
        );
    }

    // Update session status
    public int updateStatus(String status, int id) {

        return jdbcTemplate.update(
                "UPDATE sessions SET status = ? WHERE session_id = ?",
                status, id
        );
    }
}
