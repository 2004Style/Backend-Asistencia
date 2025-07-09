package com.systems.config;

import com.systems.model.*;
import com.systems.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final IRoleRepo roleRepo;
    private final IUserRepo userRepo;
    private final IPersonRepo personRepo;
    private final ITeacherRepo teacherRepo;
    private final IStudentRepo studentRepo;
    private final ISubjectRepo subjectRepo;
    private final IClassroomRepo classroomRepo;
    private final IScheduleRepo scheduleRepo;
    private final IEnrollmentRepo enrollmentRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // La lógica de siembra se ejecutará solo si no hay datos para evitar
        // duplicados.
        if (roleRepo.count() == 0) {
            seedRoles();
        }
        if (userRepo.count() == 0) {
            seedUsers();
        }
        if (subjectRepo.count() == 0) {
            seedSubjectsAndClassrooms();
        }
        if (enrollmentRepo.count() == 0) {
            seedEnrollments();
        }
    }

    private void seedRoles() {
        System.out.println("Seeding roles...");

        Role adminRole = new Role();
        adminRole.setIdRole(1);
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrador del sistema");
        roleRepo.save(adminRole);

        Role teacherRole = new Role();
        teacherRole.setIdRole(2);
        teacherRole.setName("TEACHER");
        teacherRole.setDescription("Profesor");
        roleRepo.save(teacherRole);

        Role studentRole = new Role();
        studentRole.setIdRole(3);
        studentRole.setName("STUDENT");
        studentRole.setDescription("Estudiante");
        roleRepo.save(studentRole);

        System.out.println("Roles seeded.");
    }

    private void seedUsers() {
        System.out.println("Seeding users...");
        // --- Admin User ---
        Person adminPerson = createPerson("Admin", "User", "admin@mail.com", "11111111");
        createUser(adminPerson, "admin", "password", "ADMIN");

        // --- Teacher User ---
        Person teacherPerson = createPerson("Profesor", "Uno", "teacher@mail.com", "22222222");
        createUser(teacherPerson, "teacher", "password", "TEACHER");
        Teacher teacher = new Teacher();
        teacher.setPerson(teacherPerson);
        teacherRepo.save(teacher);

        // --- Student User ---
        Person studentPerson = createPerson("Estudiante", "Uno", "student@mail.com", "33333333");
        createUser(studentPerson, "student", "password", "STUDENT");
        Student student = new Student();
        student.setPerson(studentPerson);
        studentRepo.save(student);
        System.out.println("Users seeded.");
    }

    private void seedSubjectsAndClassrooms() {
        System.out.println("Seeding subjects and classrooms...");
        // --- Subjects ---
        subjectRepo.save(new Subject(null, "Matemáticas Avanzadas"));
        subjectRepo.save(new Subject(null, "Historia del Siglo XX"));
        Subject programming = subjectRepo.save(new Subject(null, "Programación Orientada a Objetos"));

        // --- Classroom ---
        Teacher teacher = teacherRepo.findAll().get(0); // Get the first teacher created
        Classroom classroom = new Classroom();
        classroom.setName("Salón 101");
        classroom.setTeacher(teacher);
        classroom.setSubject(programming);
        Classroom savedClassroom = classroomRepo.save(classroom);

        // --- Schedule for the classroom ---
        Schedule schedule = new Schedule();
        schedule.setIdSchedule(1); // ID asignado manualmente
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(10, 0));
        schedule.setClassroom(savedClassroom);
        scheduleRepo.save(schedule);
        System.out.println("Subjects and classrooms seeded.");
    }

    private void seedEnrollments() {
        System.out.println("Seeding enrollments...");
        // --- Enrollment ---
        Student student = studentRepo.findAll().get(0);
        Classroom classroom = classroomRepo.findAll().get(0);

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setClassroom(classroom);
        enrollmentRepo.save(enrollment);
        System.out.println("Enrollments seeded.");
    }

    private Person createPerson(String firstName, String lastName, String email, String dni) {
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmail(email);
        person.setDni(dni);
        person.setBirthdate(LocalDate.of(1990, 1, 1));
        person.setGender("N/A");
        person.setAddress("123 Main St");
        person.setPhone("555-1234");
        return personRepo.save(person);
    }

    private User createUser(Person person, String username, String rawPassword, String roleName) {
        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.setRoles(List.of(role));
        user.setPerson(person);
        return userRepo.save(user);
    }
}
