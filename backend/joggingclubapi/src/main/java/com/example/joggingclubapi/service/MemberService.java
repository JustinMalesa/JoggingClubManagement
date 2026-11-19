
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
import org.springframework.web.server.ResponseStatusException;

import com.example.joggingclubapi.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // Retrieve all members
    public List<Map<String, Object>> getAllMembers() {
        return memberRepository.findAll();
    }

    // Register a new member
    public void registerMember(Map<String, Object> member) {

        String fullName = getRequiredText(member, "full_name");
        String email = getRequiredText(member, "email");
        String phone = getRequiredText(member, "phone");
        String emergencyName = getRequiredText(member, "emergency_name");
        String emergencyPhone = getRequiredText(member, "emergency_phone");

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please provide a valid email address."
            );
        }

        try {
            memberRepository.save(
                    fullName,
                    email,
                    phone,
                    emergencyName,
                    emergencyPhone
            );

        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email address is already registered."
            );
        }
    }

    // Check that required fields are supplied
    private String getRequiredText(Map<String, Object> member, String key) {

        Object value = member.get(key);

        if (value == null || value.toString().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    key + " is required."
            );
        }

        return value.toString().trim();
    }
}