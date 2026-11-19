
package com.example.joggingclubapi.repository;

/**
 *
 * @author justi
 */

import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    public MemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Retrieve all members
    public List<Map<String, Object>> findAll() {

        String sql = "SELECT member_id, full_name, email, phone, "
                + "emergency_name, emergency_phone, created_at "
                + "FROM members ORDER BY member_id DESC";

        return jdbcTemplate.queryForList(sql);
    }

    // Save a new member
    public int save(
            String fullName,
            String email,
            String phone,
            String emergencyName,
            String emergencyPhone) {

        String sql = "INSERT INTO members "
                + "(full_name, email, phone, emergency_name, emergency_phone) "
                + "VALUES (?, ?, ?, ?, ?)";

        return jdbcTemplate.update(
                sql,
                fullName,
                email,
                phone,
                emergencyName,
                emergencyPhone
        );
    }
}