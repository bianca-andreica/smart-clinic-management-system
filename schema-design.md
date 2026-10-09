# Smart Clinic Management System — MySQL Schema Design

## 1. Overview

The Smart Clinic Management System uses MySQL as its relational database. The database stores administrator accounts, doctors, patients, appointments, and prescriptions.

The schema uses primary keys to identify records and foreign keys to maintain referential integrity between related tables.

## 2. Database Design

Database name: `smart_clinic_db`

### Table 1: admins

Stores administrator account information.

| Column        | Data type    | Constraints                         |
| ------------- | ------------ | ----------------------------------- |
| id            | BIGINT       | Primary key, auto-increment         |
| full_name     | VARCHAR(100) | NOT NULL                            |
| email         | VARCHAR(150) | NOT NULL, UNIQUE                    |
| password_hash | VARCHAR(255) | NOT NULL                            |
| created_at    | TIMESTAMP    | NOT NULL, default CURRENT_TIMESTAMP |

### Table 2: doctors

Stores doctor profiles, login credentials, specialties, and availability.

| Column          | Data type    | Constraints                         |
| --------------- | ------------ | ----------------------------------- |
| id              | BIGINT       | Primary key, auto-increment         |
| full_name       | VARCHAR(100) | NOT NULL                            |
| email           | VARCHAR(150) | NOT NULL, UNIQUE                    |
| phone           | VARCHAR(20)  | UNIQUE                              |
| specialty       | VARCHAR(100) | NOT NULL                            |
| password_hash   | VARCHAR(255) | NOT NULL                            |
| available_times | JSON         | NOT NULL                            |
| created_at      | TIMESTAMP    | NOT NULL, default CURRENT_TIMESTAMP |

The `available_times` column stores a JSON representation of the doctor's availability schedule.

### Table 3: patients

Stores patient profiles and login information.

| Column        | Data type    | Constraints                         |
| ------------- | ------------ | ----------------------------------- |
| id            | BIGINT       | Primary key, auto-increment         |
| full_name     | VARCHAR(100) | NOT NULL                            |
| email         | VARCHAR(150) | NOT NULL, UNIQUE                    |
| phone         | VARCHAR(20)  | UNIQUE                              |
| password_hash | VARCHAR(255) | NOT NULL                            |
| date_of_birth | DATE         | NULL                                |
| created_at    | TIMESTAMP    | NOT NULL, default CURRENT_TIMESTAMP |

Patient records can be searched using email or phone number.

### Table 4: appointments

Stores appointments booked between doctors and patients.

| Column           | Data type                                | Constraints                         |
| ---------------- | ---------------------------------------- | ----------------------------------- |
| id               | BIGINT                                   | Primary key, auto-increment         |
| doctor_id        | BIGINT                                   | NOT NULL, foreign key               |
| patient_id       | BIGINT                                   | NOT NULL, foreign key               |
| appointment_time | DATETIME                                 | NOT NULL                            |
| reason           | TEXT                                     | NULL                                |
| status           | ENUM('BOOKED', 'COMPLETED', 'CANCELLED') | NOT NULL, default 'BOOKED'          |
| created_at       | TIMESTAMP                                | NOT NULL, default CURRENT_TIMESTAMP |

Foreign key relationships:

* `doctor_id` references `doctors(id)`.
* `patient_id` references `patients(id)`.

The `appointment_time` column stores both the appointment date and time. Application-level validation checks doctor availability and prevents conflicting bookings.

### Table 5: prescriptions

Stores prescriptions associated with appointments.

| Column         | Data type    | Constraints                         |
| -------------- | ------------ | ----------------------------------- |
| id             | BIGINT       | Primary key, auto-increment         |
| appointment_id | BIGINT       | NOT NULL, foreign key               |
| medication     | VARCHAR(255) | NOT NULL                            |
| dosage         | VARCHAR(100) | NOT NULL                            |
| instructions   | TEXT         | NULL                                |
| prescribed_at  | TIMESTAMP    | NOT NULL, default CURRENT_TIMESTAMP |

Foreign key relationship:

* `appointment_id` references `appointments(id)`.

The associated appointment identifies the doctor and patient for the prescription.

## 3. Relationships

* One doctor can have multiple appointments.
* One patient can book multiple appointments.
* Each appointment belongs to one doctor and one patient.
* Each prescription belongs to an appointment.
* One appointment may have multiple prescriptions.
* Administrators manage doctor records through authorized application functions.

## 4. MySQL Schema SQL

```sql
CREATE DATABASE IF NOT EXISTS smart_clinic_db;
USE smart_clinic_db;

CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20) UNIQUE,
    specialty VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    available_times JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    date_of_birth DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    appointment_time DATETIME NOT NULL,
    reason TEXT NULL,
    status ENUM('BOOKED', 'COMPLETED', 'CANCELLED')
        NOT NULL DEFAULT 'BOOKED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_appointments_doctor_time
        (doctor_id, appointment_time),
    INDEX idx_appointments_patient_time
        (patient_id, appointment_time),
    CONSTRAINT fk_appointments_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_appointments_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE prescriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    medication VARCHAR(255) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    instructions TEXT NULL,
    prescribed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prescriptions_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;
```

## 5. Reporting Support

The appointments table supports the required reporting procedures:

* `GetDailyAppointmentReportByDoctor` retrieves appointment information for a doctor on a selected date.
* `GetDoctorWithMostPatientsByMonth` identifies the doctor with the most distinct patients during a selected month.
* `GetDoctorWithMostPatientsByYear` identifies the doctor with the most distinct patients during a selected year.

The `doctor_id`, `patient_id`, and `appointment_time` columns support the joins, filtering, and grouping needed by these reports.

## 6. Data Integrity and Security

* Primary keys uniquely identify database records.
* Unique constraints prevent duplicate email addresses.
* Foreign keys enforce relationships between doctors, patients, appointments, and prescriptions.
* Required fields use `NOT NULL` constraints.
* Passwords are stored as hashes, not plaintext.
* Application-level validation and authorization protect appointment and prescription operations.
* InnoDB provides foreign key enforcement and transactional support.
