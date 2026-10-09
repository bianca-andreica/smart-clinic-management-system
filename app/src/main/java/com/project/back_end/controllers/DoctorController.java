package com.project.back_end.controllers;

import com.project.back_end.services.DoctorService;
import com.project.back_end.services.TokenService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final TokenService tokenService;

    public DoctorController(
            DoctorService doctorService,
            TokenService tokenService) {
        this.doctorService = doctorService;
        this.tokenService = tokenService;
    }

    @GetMapping("/{doctorId}/availability")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable Long doctorId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization) {

        Map<String, Object> response = new LinkedHashMap<>();

        if (authorization == null || authorization.isBlank()) {
            response.put("success", false);
            response.put("message", "Authorization token is required");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7).trim()
                : authorization.trim();

        if (token.isBlank() || !tokenService.validateToken(token)) {
            response.put("success", false);
            response.put("message", "Invalid or expired token");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        List<String> availableTimes =
                doctorService.getAvailableTimes(doctorId, date);

        response.put("success", true);
        response.put("message", "Doctor availability retrieved successfully");
        response.put("doctorId", doctorId);
        response.put("date", date);
        response.put("availableTimes", availableTimes);

        return ResponseEntity.ok(response);
    }
}
