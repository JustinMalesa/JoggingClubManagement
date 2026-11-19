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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.joggingclubapi.service.MemberService;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // Retrieve all members
    @GetMapping("/api/members")
    public List<Map<String, Object>> getMembers() {
        return memberService.getAllMembers();
    }

    // Register a new member
    @PostMapping("/api/members")
    public ResponseEntity<String> addMember(
            @RequestBody Map<String, Object> member) {

        try {
            memberService.registerMember(member);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Member registered successfully.");

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getReason());
        }
    }
}