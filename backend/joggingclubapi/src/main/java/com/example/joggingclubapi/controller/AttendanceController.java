package com.example.joggingclubapi.controller;

/**
 *
 * @author justi
 */

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.joggingclubapi.service.AttendanceService;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // Retrieve attendance records for a session
    @GetMapping("/sessions/{sessionId}/attendance")
    public ResponseEntity<?> getAttendance(
            @PathVariable int sessionId) {

        try {
            List<Map<String, Object>> results =
                    attendanceService.getAttendance(sessionId);

            return ResponseEntity.ok(results);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error",
                            "Could not retrieve attendance."));
        }
    }

    // Update attendance for a registration
    @PutMapping("/attendance/{registrationId}")
    public ResponseEntity<?> updateAttendance(
            @PathVariable int registrationId,
            @RequestBody Map<String, Object> body) {

        try {
            attendanceService.updateAttendance(registrationId, body);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Attendance saved successfully.");
            response.put("registration_id", registrationId);
            response.put("attended", body.get("attended"));

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not save attendance."));
        }
    }
}