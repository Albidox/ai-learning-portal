package com.ailearningportal.service;

import com.ailearningportal.dto.MarkRequest;
import com.ailearningportal.dto.StoredFile;
import com.ailearningportal.dto.SubmissionRequest;
import com.ailearningportal.entity.Assignment;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Mark;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.Submission;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.AssignmentRepository;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.MarkRepository;
import com.ailearningportal.repository.SubmissionRepository;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.impl.SubmissionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubmissionServiceTests {

    @Test
    void submitAssignmentShouldSaveTextAnswer() {
        TestContext context = context();
        SubmissionRequest request = new SubmissionRequest();
        request.setAnswerText(" My answer ");

        prepareValidSubmit(context, request);

        Submission submission = context.service.submitAssignment(10L, request, "student@example.test");

        assertEquals("My answer", submission.getAnswerText());
    }

    @Test
    void submitAssignmentShouldSaveFileAnswer() {
        TestContext context = context();
        SubmissionRequest request = new SubmissionRequest();
        request.setFile(new MockMultipartFile("file", "answer.txt", "text/plain", "hello".getBytes()));

        prepareValidSubmit(context, request);
        when(context.fileStorageService.storeSubmissionFile(any(), any())).thenReturn(new StoredFile("uploads/assignments/1_answer.txt", "answer.txt"));

        Submission submission = context.service.submitAssignment(10L, request, "student@example.test");

        assertEquals("answer.txt", submission.getOriginalFileName());
    }

    @Test
    void submitAssignmentShouldBlockEmptyAnswer() {
        TestContext context = context();
        SubmissionRequest request = new SubmissionRequest();

        prepareValidSubmit(context, request);

        assertThrows(
                IllegalArgumentException.class,
                () -> context.service.submitAssignment(10L, request, "student@example.test")
        );
        verify(context.submissionRepository, never()).save(any());
    }

    @Test
    void submitAssignmentShouldBlockDuplicateSubmission() {
        TestContext context = context();
        SubmissionRequest request = new SubmissionRequest();
        request.setAnswerText("Answer");

        prepareValidSubmit(context, request);
        when(context.submissionRepository.existsByAssignmentIdAndStudentId(10L, 2L)).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> context.service.submitAssignment(10L, request, "student@example.test")
        );
    }

    @Test
    void submitAssignmentShouldBlockLateSubmission() {
        TestContext context = context();
        SubmissionRequest request = new SubmissionRequest();
        request.setAnswerText("Answer");

        prepareValidSubmit(context, request);
        Assignment assignment = assignment();
        assignment.setDueDate(LocalDate.now().minusDays(1));
        when(context.assignmentRepository.findByIdWithCourseAndTeacher(10L)).thenReturn(Optional.of(assignment));

        assertThrows(
                IllegalArgumentException.class,
                () -> context.service.submitAssignment(10L, request, "student@example.test")
        );
    }

    @Test
    void saveMarkShouldBlockMarksAboveMaxMarks() {
        TestContext context = context();
        Submission submission = submission();
        MarkRequest request = new MarkRequest();
        request.setMarksAwarded(30);

        when(context.userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher()));
        when(context.submissionRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(submission));

        assertThrows(
                IllegalArgumentException.class,
                () -> context.service.saveMark(50L, request, "teacher@example.test")
        );
        verify(context.markRepository, never()).save(any());
    }

    @Test
    void saveMarkShouldSaveValidMarks() {
        TestContext context = context();
        Submission submission = submission();
        MarkRequest request = new MarkRequest();
        request.setMarksAwarded(18);
        request.setFeedback("Good work");

        when(context.userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher()));
        when(context.submissionRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(submission));
        when(context.markRepository.findBySubmissionId(50L)).thenReturn(Optional.empty());
        when(context.markRepository.save(any(Mark.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mark mark = context.service.saveMark(50L, request, "teacher@example.test");

        assertEquals(18, mark.getMarksAwarded());
        assertEquals("Good work", mark.getFeedback());
    }

    private void prepareValidSubmit(TestContext context, SubmissionRequest request) {
        when(context.userRepository.findByEmail("student@example.test")).thenReturn(Optional.of(student()));
        when(context.assignmentRepository.findByIdWithCourseAndTeacher(10L)).thenReturn(Optional.of(assignment()));
        when(context.enrollmentRepository.existsByStudentAndCourseAndStatus(2L, 5L, EnrollmentStatus.ACTIVE))
                .thenReturn(true);
        when(context.submissionRepository.existsByAssignmentIdAndStudentId(10L, 2L)).thenReturn(false);
        when(context.submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission submission = invocation.getArgument(0);
            if (submission.getId() == null) {
                submission.setId(50L);
            }
            return submission;
        });
    }

    private TestContext context() {
        AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        FileStorageService fileStorageService = mock(FileStorageService.class);
        MarkRepository markRepository = mock(MarkRepository.class);
        SubmissionRepository submissionRepository = mock(SubmissionRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        SubmissionService service = new SubmissionServiceImpl(
                assignmentRepository,
                enrollmentRepository,
                fileStorageService,
                markRepository,
                submissionRepository,
                userRepository
        );

        return new TestContext(
                service,
                assignmentRepository,
                enrollmentRepository,
                fileStorageService,
                markRepository,
                submissionRepository,
                userRepository
        );
    }

    private Assignment assignment() {
        Assignment assignment = new Assignment();
        assignment.setId(10L);
        assignment.setCourse(course());
        assignment.setTeacher(teacher());
        assignment.setTitle("Java Assignment");
        assignment.setDueDate(LocalDate.now().plusDays(7));
        assignment.setMaxMarks(20);
        assignment.setActive(true);
        return assignment;
    }

    private Submission submission() {
        Submission submission = new Submission();
        submission.setId(50L);
        submission.setAssignment(assignment());
        submission.setStudent(student());
        return submission;
    }

    private Course course() {
        Course course = new Course();
        course.setId(5L);
        course.setName("Java Basics");
        course.setActive(true);
        return course;
    }

    private User teacher() {
        User teacher = new User();
        teacher.setId(1L);
        teacher.setRole(Role.TEACHER);
        return teacher;
    }

    private User student() {
        User student = new User();
        student.setId(2L);
        student.setRole(Role.STUDENT);
        return student;
    }

    private record TestContext(
            SubmissionService service,
            AssignmentRepository assignmentRepository,
            EnrollmentRepository enrollmentRepository,
            FileStorageService fileStorageService,
            MarkRepository markRepository,
            SubmissionRepository submissionRepository,
            UserRepository userRepository
    ) {
    }
}
