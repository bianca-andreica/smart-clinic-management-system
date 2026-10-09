
package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.repositories.AppointmentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Saves a new appointment in the database.
     */
    @Transactional
    public Appointment bookAppointment(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException(
                    "Appointment cannot be null");
        }

        if (appointment.getDoctor() == null) {
            throw new IllegalArgumentException(
                    "Doctor is required");
        }

        if (appointment.getPatient() == null) {
            throw new IllegalArgumentException(
                    "Patient is required");
        }

        if (appointment.getAppointmentTime() == null) {
            throw new IllegalArgumentException(
                    "Appointment time is required");
        }

        return appointmentRepository.save(appointment);
    }

    /**
     * Retrieves all appointments for a doctor on a specific date.
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsForDoctorOnDate(
            Long doctorId,
            LocalDate date) {

        if (doctorId == null || date == null) {
            throw new IllegalArgumentException(
                    "Doctor ID and date are required");
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime startOfNextDay = date.plusDays(1).atStartOfDay();

        return appointmentRepository
                .findByDoctor_IdAndAppointmentTimeGreaterThanEqualAndAppointmentTimeLessThan(
                        doctorId,
                        startOfDay,
                        startOfNextDay);
    }
}
