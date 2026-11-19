package com.example.joggingclubapi.controller;

/**
 *
 * @author justi
 */

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.joggingclubapi.service.SessionService;

@RestController
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // Retrieve all sessions
    @GetMapping("/api/sessions")
    public List<Map<String, Object>> getSessions() {
        return sessionService.getAllSessions();
    }

    // Save a new session
    @PostMapping("/api/sessions")
    public ResponseEntity<String> addSession(
            @RequestBody Map<String, Object> session) {

        try {
            sessionService.addSession(session);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Session saved successfully.");

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getReason());
        }
    }

    // Edit an existing session
    @PutMapping("/api/sessions/{id}")
    public ResponseEntity<String> updateSession(
            @PathVariable int id,
            @RequestBody Map<String, Object> session) {

        try {
            sessionService.updateSession(id, session);

            return ResponseEntity.ok("Session updated successfully.");

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getReason());
        }
    }

    // Delete a session
    @DeleteMapping("/api/sessions/{id}")
    public ResponseEntity<String> deleteSession(@PathVariable int id) {

        try {
            sessionService.deleteSession(id);

            return ResponseEntity.ok("Session deleted successfully.");

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getReason());
        }
    }

    // Update a session's status
    @PutMapping("/api/sessions/{id}/status")
    public ResponseEntity<String> updateSessionStatus(
            @PathVariable int id,
            @RequestBody Map<String, Object> request) {

        try {
            sessionService.updateSessionStatus(id, request);

            return ResponseEntity.ok(
                    "Session status updated successfully."
            );

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getReason());
        }
    }
}