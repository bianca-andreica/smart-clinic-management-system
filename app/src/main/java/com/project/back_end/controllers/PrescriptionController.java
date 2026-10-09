
package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.TokenService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final TokenService tokenService;

    public PrescriptionController(
            PrescriptionService prescriptionService,
            TokenService tokenService) {
        this.prescriptionService = prescriptionService;
        this.tokenService = tokenService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPrescription(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @Valid @RequestBody Prescription prescription,
            BindingResult bindingResult) {

        Map<String, Object> response = new LinkedHashMap<>();

        // Check that an authorization token was provided.
        if (authorization == null || authorization.isBlank()) {
            response.put("success", false);
            response.put("message", "Authorization token is required");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        // Support the standard Bearer token format.
        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7).trim()
                : authorization.trim();

        // Reject invalid or expired tokens.
        if (token.isBlank() || !tokenService.validateToken(token)) {
            response.put("success", false);
            response.put("message", "Invalid or expired token");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        // Return structured validation errors for invalid request data.
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();

            response.put("success", false);
            response.put("message", "Prescription validation failed");
            response.put("errors", errors);

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        try {
            Prescription savedPrescription =
                    prescriptionService.savePrescription(prescription);

            response.put("success", true);
            response.put("message", "Prescription saved successfully");
            response.put("prescription", savedPrescription);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (IllegalArgumentException exception) {
            response.put("success", false);
            response.put("message", exception.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }
}
