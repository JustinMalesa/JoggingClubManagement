
package com.example.joggingclubapi.service;

/**
 *
 * @author justi
 */

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.joggingclubapi.repository.AttendanceRepository;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    // Retrieve attendance records for a session
    public List<Map<String, Object>> getAttendance(int sessionId) {

        if (sessionId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Session ID must be greater than zero."
            );
        }

        return attendanceRepository.findAttendance(sessionId);
    }

    // Save or update attendance
    public void updateAttendance(
            int registrationId, Map<String, Object> body) {

        if (registrationId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Registration ID must be greater than zero."
            );
        }

        Object value = body.get("attended");

        if (!(value instanceof Boolean)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "attended must be true or false."
            );
        }

        boolean attended = (Boolean) value;

        int rows = attendanceRepository.saveAttendance(
                registrationId, attended
        );

        if (rows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Registration not found."
            );
        }
    }
}