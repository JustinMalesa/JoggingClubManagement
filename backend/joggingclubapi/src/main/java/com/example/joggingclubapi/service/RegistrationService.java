
package com.example.joggingclubapi.service;

/**
 *
 * @author justi
 */

import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.joggingclubapi.repository.RegistrationRepository;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;

    public RegistrationService(
            RegistrationRepository registrationRepository) {

        this.registrationRepository = registrationRepository;
    }

    // Register a member for a jogging session
    @Transactional
    public void registerMember(Map<String, Object> data) {

        int memberId = getRequiredId(data, "member_id");
        int sessionId = getRequiredId(data, "session_id");

        // Check whether the session exists
        List<Map<String, Object>> sessions =
                registrationRepository.findSessionForUpdate(sessionId);

        if (sessions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Session not found."
            );
        }

        Map<String, Object> session = sessions.get(0);

        String status = String.valueOf(session.get("status"));
        int capacity = ((Number) session.get("capacity")).intValue();

        if (!"Open".equalsIgnoreCase(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This session is not open for registration."
            );
        }

        // Check whether the member exists
        Integer memberCount =
                registrationRepository.countMembers(memberId);

        if (memberCount == null || memberCount == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Member not found."
            );
        }

        // Check available capacity
        Integer registrationCount =
                registrationRepository.countRegistrations(sessionId);

        if (registrationCount != null
                && registrationCount >= capacity) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This session has reached capacity."
            );
        }

        try {
            // Save the registration
            registrationRepository.save(memberId, sessionId);

        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This member is already registered for this session."
            );
        }

        // Mark the session as full if capacity has been reached
        if (registrationCount != null
                && registrationCount + 1 >= capacity) {

            registrationRepository.updateSessionStatus(sessionId);
        }
    }

    // Retrieve participants for a session
    public List<Map<String, Object>> getParticipants(int sessionId) {

        return registrationRepository.findParticipants(sessionId);
    }

    // Validate member and session IDs
    private int getRequiredId(Map<String, Object> data, String key) {

        Object value = data.get(key);

        if (value == null || value.toString().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, key + " is required."
            );
        }

        try {
            int id = Integer.parseInt(value.toString());

            if (id <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        key + " must be greater than zero."
                );
            }

            return id;

        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    key + " must be a valid number."
            );
        }
    }
}