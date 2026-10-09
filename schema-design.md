
# Smart Clinic Management System — Database Schema Design

## 1. Overview

The Smart Clinic Management System uses MySQL for relational data and MongoDB for prescription documents.

The MySQL database is named `cms`, as specified in the course lab. Spring Boot and JPA are used to create and manage the relational tables.

The relational database contains the following five tables:
- `admin`
- `doctor`
- `doctor_available_times`
- `patient`
- `appointment`

Prescription documents are stored separately in the MongoDB `prescriptions` collection.

## 2. MySQL Database

Database name: `cms`

### 2.1 Table: doctor

Stores doctor profiles, contact information, login credentials, and medical specialties.

| Column | Data type | Constraints |
|---|---|---|
| id | BIGINT | Primary key, auto-increment |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| name | VARCHAR(255) | NOT NULL |
| password | VARCHAR(255) | NOT NULL |
| phone | VARCHAR(30) | NULL |
| specialty | VARCHAR(100) | NOT NULL |

### 2.2 Table: doctor_available_times

Stores the available appointment time slots for each doctor. A doctor can have multiple availability entries.

| Column | Data type | Constraints |
|---|---|---|
| doctor_id | BIGINT | NOT NULL, foreign key |
| available_times | VARCHAR(50) | NOT NULL |

Relationship:
- `doctor_id` references `doctor(id)`.
- One doctor can have multiple availability time slots.
- The combination of `doctor_id` and `available_times` can be used as a composite primary key to prevent duplicate availability entries.

Example availability values include `09:00-10:00` and `10:00-11:00`.

### 2.3 Table: patient

Stores patient profiles and login information.

| Column | Data type | Constraints |
|---|---|---|
| id | BIGINT | Primary key, auto-increment |
| address | VARCHAR(255) | NULL |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| name | VARCHAR(255) | NOT NULL |
| password | VARCHAR(255) | NOT NULL |
| phone | VARCHAR(30) | NULL |

Each patient is identified by a unique database ID. Email is unique to support patient lookup and login.

### 2.4 Table: appointment

Stores appointments linking doctors and patients.

| Column | Data type | Constraints |
|---|---|---|
| id | BIGINT | Primary key, auto-increment |
| appointment_time | DATETIME(6) | NOT NULL |
| status | TINYINT | NOT NULL, default 0 |
| doctor_id | BIGINT | NOT NULL, foreign key |
| patient_id | BIGINT | NOT NULL, foreign key |

Foreign key relationships:
- `doctor_id` references `doctor(id)`.
- `patient_id` references `patient(id)`.

One doctor can have multiple appointments, and one patient can have multiple appointments. Each appointment belongs to one doctor and one patient.

The course sample data uses numeric status values such as `0` and `1`. Their exact meaning depends on the application model.

### 2.5 Table: admin

Stores administrator login information.

| Column | Data type | Constraints |
|---|---|---|
| id | BIGINT | Primary key, auto-increment |
| username | VARCHAR(100) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL |

Administrators use the admin account to access administrative functionality, including doctor management.

## 3. Entity Relationships

The main relationships are:

- `doctor` 1-to-many `doctor_available_times`
- `doctor` 1-to-many `appointment`
- `patient` 1-to-many `appointment`

The `doctor_available_times` table references the doctor whose availability is recorded.

The `appointment` table contains foreign keys to both `doctor` and `patient`, ensuring that each appointment is associated with valid records.

## 4. SQL Schema Reference

The following SQL illustrates the relational database design. In the course lab, Spring Boot and JPA create the tables when the backend starts, so the actual table definitions must remain consistent with the entity classes.

```sql
CREATE DATABASE IF NOT EXISTS cms;
USE cms;

CREATE TABLE doctor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    specialty VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE doctor_available_times (
    doctor_id BIGINT NOT NULL,
    available_times VARCHAR(50) NOT NULL,
    PRIMARY KEY (doctor_id, available_times),
    CONSTRAINT fk_available_times_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctor(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE patient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    address VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(30)
) ENGINE=InnoDB;

CREATE TABLE admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_time DATETIME(6) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    INDEX idx_appointment_doctor_time
        (doctor_id, appointment_time),
    INDEX idx_appointment_patient_time
        (patient_id, appointment_time),
    CONSTRAINT fk_appointment_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    CONSTRAINT fk_appointment_patient
        FOREIGN KEY (patient_id) REFERENCES patient(id)
) ENGINE=InnoDB;
```

## 5. MongoDB Prescription Documents

Prescriptions are stored in a separate MongoDB collection named `prescriptions`, rather than in a MySQL table.

A prescription document can contain these fields:

| Field | Type | Description |
|---|---|---|
| _id | ObjectId | Unique document identifier |
| patientName | String | Patient name |
| appointmentId | Number | Associated appointment ID |
| medication | String | Medication name |
| dosage | String | Prescribed dosage |
| doctorNotes | String | Doctor's instructions |
| _class | String | Spring Data document type metadata |

The `appointmentId` links the prescription logically to an appointment in MySQL. Application logic is responsible for maintaining that relationship across the two databases.

## 6. Data Integrity and Security

- Primary keys uniquely identify records.
- Unique constraints prevent duplicate doctor emails, patient emails, and administrator usernames.
- Foreign keys maintain relationships between doctors, patients, availability slots, and appointments.
- Required fields use `NOT NULL` constraints.
- Application-level validation checks appointment data and doctor availability.
- In a production deployment, passwords must be stored as secure hashes rather than plaintext.
- Database credentials must be configured outside public source code and must not be committed to GitHub.

## 7. Reporting Support

The `appointment` table provides the doctor IDs, patient IDs, appointment dates, and status values needed for appointment reporting.

The stored procedures `GetDailyAppointmentReportByDoctor`, `GetDoctorWithMostPatientsByMonth`, and `GetDoctorWithMostPatientsByYear` can use joins between `appointment`, `doctor`, and `patient` to generate the required reports.
