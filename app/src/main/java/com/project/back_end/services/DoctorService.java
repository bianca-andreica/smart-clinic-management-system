
package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repositories.AppointmentRepository;
import com.project.back_end.repositories.DoctorRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public DoctorService(
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /**
     * Returns available appointment slots for a doctor on a specific date.
     * Availability entries must use ISO date-time format, for example:
     * 2026-10-12T09:00
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableTimes(Long doctorId, LocalDate date) {

        if (doctorId == null || date == null) {
            throw new IllegalArgumentException(
                    "Doctor ID and date are required");
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Doctor not found"));

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime startOfNextDay =
                date.plusDays(1).atStartOfDay();

        List<Appointment> appointments =
                appointmentRepository
                        .findByDoctor_IdAndAppointmentTimeGreaterThanEqualAndAppointmentTimeLessThan(
                                doctorId,
                                startOfDay,
                                startOfNextDay);

        Set<LocalDateTime> bookedTimes = appointments.stream()
                .filter(appointment ->
                        appointment.getStatus()
                                != Appointment.AppointmentStatus.CANCELLED)
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        return doctor.getAvailableTimes().stream()
                .filter(slot -> {
                    try {
                        LocalDateTime slotDateTime =
                                LocalDateTime.parse(slot);

                        return slotDateTime.toLocalDate().equals(date)
                                && !bookedTimes.contains(slotDateTime);

                    } catch (DateTimeParseException exception) {
                        return false;
                    }
                })
                .sorted()
                .toList();
    }

    /**
     * Validates doctor login credentials and returns a structured response.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> validateDoctorLogin(
            String email,
            String password) {

        Map<String, Object> response = new LinkedHashMap<>();

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            response.put("success", false);
            response.put("message", "Email and password are required");
            return response;
        }

        Optional<Doctor> optionalDoctor =
                doctorRepository.findByEmail(email.trim());

        if (optionalDoctor.isEmpty()) {
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return response;
        }

        Doctor doctor = optionalDoctor.get();

        if (!passwordEncoder.matches(
                password, doctor.getPasswordHash())) {
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return response;
        }

        String token = tokenService.generateToken(doctor.getEmail());

        response.put("success", true);
        response.put("message", "Doctor login successful");
        response.put("token", token);
        response.put("doctorId", doctor.getId());
        response.put("name", doctor.getFullName());
        response.put("email", doctor.getEmail());
        response.put("specialty", doctor.getSpecialty());

        return response;
    }
}
