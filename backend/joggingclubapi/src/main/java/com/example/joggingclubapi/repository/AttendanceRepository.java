
package com.example.joggingclubapi.repository;

/**
 *
 * @author justi
 */

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AttendanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public AttendanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Retrieve attendance records for a session
    public List<Map<String, Object>> findAttendance(int sessionId) {

        String sql = "SELECT r.registration_id, m.member_id, "
                + "m.full_name, m.email, a.attended, a.recorded_at "
                + "FROM registrations r "
                + "JOIN members m ON r.member_id = m.member_id "
                + "LEFT JOIN attendance a "
                + "ON r.registration_id = a.registration_id "
                + "WHERE r.session_id = ? "
                + "ORDER BY m.full_name";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            Map<String, Object> row = new LinkedHashMap<>();

            row.put("registration_id", rs.getInt("registration_id"));
            row.put("member_id", rs.getInt("member_id"));
            row.put("full_name", rs.getString("full_name"));
            row.put("email", rs.getString("email"));

            Object attended = rs.getObject("attended");

            row.put("attended",
                    attended == null ? null : rs.getBoolean("attended"));

            row.put("recorded_at", rs.getTimestamp("recorded_at"));

            return row;
        }, sessionId);
    }

    // Save or update attendance
    public int saveAttendance(int registrationId, boolean attended) {

        String sql = "INSERT INTO attendance (registration_id, attended) "
                + "SELECT registration_id, ? FROM registrations "
                + "WHERE registration_id = ? "
                + "ON DUPLICATE KEY UPDATE "
                + "attended = VALUES(attended), "
                + "recorded_at = CURRENT_TIMESTAMP";

        return jdbcTemplate.update(sql, attended, registrationId);
    }
}
