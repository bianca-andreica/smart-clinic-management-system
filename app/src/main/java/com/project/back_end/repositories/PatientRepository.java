
package com.project.back_end.repositories;

import com.project.back_end.models.Patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Find a patient by their email address.
    Optional<Patient> findByEmail(String email);

    // Find a patient by email address or phone number.
    Optional<Patient> findByEmailOrPhone(String email, String phone);
}
