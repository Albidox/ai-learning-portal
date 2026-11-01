package com.ailearningportal.config;

import com.ailearningportal.entity.Assignment;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Enrollment;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Lesson;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.Subject;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.AssignmentRepository;
import com.ailearningportal.repository.CourseRepository;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.LessonRepository;
import com.ailearningportal.repository.SubjectRepository;
import com.ailearningportal.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    private static final String SAMPLE_PASSWORD = "Password@123";
    private static final String SAMPLE_COURSE_NAME = "Java Fundamentals";
    private static final String SAMPLE_SUBJECT_NAME = "Spring Boot Basics";
    private static final String SAMPLE_LESSON_TITLE = "What Is Spring Boot?";
    private static final String SAMPLE_ASSIGNMENT_TITLE = "Write Short Notes About Spring Boot";
    private static final String SAMPLE_LESSON_CONTENT = """
            Spring Boot is a Java framework used to create web applications quickly.
            It reduces manual setup by giving auto configuration.
            It can start an embedded server, so students can run the project from Eclipse or Maven.
            In this project, Spring Boot connects controllers, services, repositories, security, pages, and PostgreSQL.
            A controller receives a browser request.
            A service contains business logic.
            A repository talks to the database.
            Thymeleaf is used to show HTML pages to admin, teacher, and student users.
            """;

    @Value("${app.sample-data.enabled:true}")
    private boolean sampleDataEnabled;

    @Bean
    public CommandLineRunner createSampleData(
            UserService userService,
            CourseRepository courseRepository,
            SubjectRepository subjectRepository,
            EnrollmentRepository enrollmentRepository,
            LessonRepository lessonRepository,
            AssignmentRepository assignmentRepository
    ) {
        return args -> {
            if (!sampleDataEnabled) {
                return;
            }

            // Local sample users for student practice.
            userService.createUserIfMissing("Admin User", "admin@example.test", SAMPLE_PASSWORD, Role.ADMIN);
            User teacher = userService.createUserIfMissing("Teacher User", "teacher@example.test", SAMPLE_PASSWORD, Role.TEACHER);
            User student = userService.createUserIfMissing("Student User", "student@example.test", SAMPLE_PASSWORD, Role.STUDENT);

            Course course = findOrCreateSampleCourse(courseRepository);
            Subject subject = findOrCreateSampleSubject(subjectRepository, course);

            createSampleEnrollmentIfMissing(enrollmentRepository, course, student);
            createSampleLessonIfMissing(lessonRepository, subject, teacher);
            createSampleAssignmentIfMissing(assignmentRepository, course, teacher);
        };
    }

    private Course findOrCreateSampleCourse(CourseRepository courseRepository) {
        return courseRepository.findByNameIgnoreCase(SAMPLE_COURSE_NAME)
                .orElseGet(() -> courseRepository.save(new Course(
                        SAMPLE_COURSE_NAME,
                        "Starter course for local student practice.",
                        LocalDate.now(),
                        LocalDate.now().plusMonths(3),
                        true
                )));
    }

    private Subject findOrCreateSampleSubject(SubjectRepository subjectRepository, Course course) {
        return subjectRepository.findByCourseIdAndNameIgnoreCase(course.getId(), SAMPLE_SUBJECT_NAME)
                .orElseGet(() -> subjectRepository.save(new Subject(
                        course,
                        SAMPLE_SUBJECT_NAME,
                        "Basic Spring Boot topics for beginners."
                )));
    }

    private void createSampleEnrollmentIfMissing(
            EnrollmentRepository enrollmentRepository,
            Course course,
            User student
    ) {
        boolean alreadyEnrolled = enrollmentRepository.existsByCourseIdAndStudentId(course.getId(), student.getId());
        if (!alreadyEnrolled) {
            enrollmentRepository.save(new Enrollment(course, student, EnrollmentStatus.ACTIVE));
        }
    }

    private void createSampleLessonIfMissing(LessonRepository lessonRepository, Subject subject, User teacher) {
        boolean lessonExists = lessonRepository.existsBySubjectIdAndTitleIgnoreCase(
                subject.getId(),
                SAMPLE_LESSON_TITLE
        );
        if (!lessonExists) {
            Lesson lesson = new Lesson(subject, teacher, SAMPLE_LESSON_TITLE, SAMPLE_LESSON_CONTENT.trim());
            lesson.setPublished(true);
            lesson.setPublishedAt(LocalDateTime.now());
            lessonRepository.save(lesson);
        }
    }

    private void createSampleAssignmentIfMissing(
            AssignmentRepository assignmentRepository,
            Course course,
            User teacher
    ) {
        boolean assignmentExists = assignmentRepository.existsByCourseIdAndTitleIgnoreCase(
                course.getId(),
                SAMPLE_ASSIGNMENT_TITLE
        );
        if (!assignmentExists) {
            assignmentRepository.save(new Assignment(
                    course,
                    teacher,
                    SAMPLE_ASSIGNMENT_TITLE,
                    "Write 5 to 8 lines explaining what Spring Boot is and why it is useful.",
                    LocalDate.now().plusDays(14),
                    20,
                    true
            ));
        }
    }
}
