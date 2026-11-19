
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

import com.example.joggingclubapi.repository.SessionRepository;
import com.example.joggingclubapi.repository.SessionRepository;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // Retrieve all sessions
    public List<Map<String, Object>> getAllSessions() {
        return sessionRepository.findAll();
    }

    // Add a new session
    public void addSession(Map<String, Object> session) {

        String name = getRequiredText(session, "session_name");
        String category = getRequiredText(session, "category");
        String date = getRequiredText(session, "session_date");
        String time = getRequiredText(session, "session_time");
        String location = getRequiredText(session, "location");
        int capacity = getCapacity(session);

        validateCategory(category);

        if (capacity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Capacity must be greater than zero."
            );
        }

        sessionRepository.save(
                name, category, date, time, location, capacity
        );
    }

    // Update an existing session
    public void updateSession(int id, Map<String, Object> session) {

        String name = getRequiredText(session, "session_name");
        String category = getRequiredText(session, "category");
        String date = getRequiredText(session, "session_date");
        String time = getRequiredText(session, "session_time");
        String location = getRequiredText(session, "location");
        int capacity = getCapacity(session);

        validateCategory(category);

        if (capacity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Capacity must be greater than zero."
            );
        }

        int rowsUpdated = sessionRepository.update(
                name, category, date, time, location, capacity, id
        );

        if (rowsUpdated == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Session not found."
            );
        }
    }

    // Delete a session
    public void deleteSession(int id) {

        Integer registrations = sessionRepository.countRegistrations(id);

        if (registrations != null && registrations > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This session has registrations and cannot be deleted."
            );
        }

        int rowsDeleted = sessionRepository.delete(id);

        if (rowsDeleted == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Session not found."
            );
        }
    }

    // Update session status
    public void updateSessionStatus(int id, Map<String, Object> request) {

        String status = getRequiredText(request, "status");

        if (!status.equals("Open")
                && !status.equals("Full")
                && !status.equals("Cancelled")
                && !status.equals("Completed")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid session status."
            );
        }

        int rowsUpdated = sessionRepository.updateStatus(status, id);

        if (rowsUpdated == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Session not found."
            );
        }
    }

    // Validate session category
    private void validateCategory(String category) {

        if (!category.equals("Beginner")
                && !category.equals("Intermediate")
                && !category.equals("Advanced")
                && !category.equals("Family Fun Run")
                && !category.equals("Marathon Training")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid session category."
            );
        }
    }

    // Check that required text fields are supplied
    private String getRequiredText(Map<String, Object> data, String key) {

        Object value = data.get(key);

        if (value == null || value.toString().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, key + " is required."
            );
        }

        return value.toString().trim();
    }

    // Read and validate session capacity
    private int getCapacity(Map<String, Object> session) {

        Object value = session.get("capacity");

        if (value == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "capacity is required."
            );
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Capacity must be a valid number."
            );
        }
    }
}