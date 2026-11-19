package com.example.joggingclubapi.controller;

/**
 *
 * @author justi
 */

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.joggingclubapi.service.RegistrationService;

@RestController
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(
            RegistrationService registrationService) {

        this.registrationService = registrationService;
    }

    // Register a member for a jogging session
    @PostMapping("/api/registrations")
    public ResponseEntity<String> registerMember(
            @RequestBody Map<String, Object> data) {

        try {
            registrationService.registerMember(data);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Member registered for the session successfully.");

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getReason());
        }
    }

    // Retrieve participants registered for a session
    @GetMapping("/api/sessions/{sessionId}/participants")
    public List<Map<String, Object>> getParticipants(
            @PathVariable int sessionId) {

        return registrationService.getParticipants(sessionId);
    }
}