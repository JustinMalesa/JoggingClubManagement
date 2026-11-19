
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
public class RegistrationRepository {

    private final JdbcTemplate jdbcTemplate;

    public RegistrationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Find a session and lock it while registration is processed
    public List<Map<String, Object>> findSessionForUpdate(int sessionId) {

        String sql = "SELECT capacity, status FROM sessions "
                + "WHERE session_id = ? FOR UPDATE";

        return jdbcTemplate.queryForList(sql, sessionId);
    }

    // Check whether a member exists
    public Integer countMembers(int memberId) {

        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM members WHERE member_id = ?",
                Integer.class,
                memberId
        );
    }

    // Count registrations for a session
    public Integer countRegistrations(int sessionId) {

        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM registrations WHERE session_id = ?",
                Integer.class,
                sessionId
        );
    }

    // Save a new registration
    public int save(int memberId, int sessionId) {

        String sql = "INSERT INTO registrations (member_id, session_id) "
                + "VALUES (?, ?)";

        return jdbcTemplate.update(sql, memberId, sessionId);
    }

    // Update the session status
    public int updateSessionStatus(int sessionId) {

        return jdbcTemplate.update(
                "UPDATE sessions SET status = 'Full' WHERE session_id = ?",
                sessionId
        );
    }

    // Retrieve participants registered for a session
    public List<Map<String, Object>> findParticipants(int sessionId) {

        String sql = "SELECT r.registration_id, r.member_id, "
                + "m.full_name, m.email, m.phone, r.registration_date "
                + "FROM registrations r "
                + "JOIN members m ON r.member_id = m.member_id "
                + "WHERE r.session_id = ? "
                + "ORDER BY r.registration_date DESC";

        return jdbcTemplate.queryForList(sql, sessionId);
    }
}

