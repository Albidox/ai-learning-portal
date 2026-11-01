package com.ailearningportal.service.impl;

import com.ailearningportal.dto.MarkRequest;
import com.ailearningportal.dto.StoredFile;
import com.ailearningportal.dto.SubmissionRequest;
import com.ailearningportal.entity.Assignment;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Mark;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.Submission;
import com.ailearningportal.entity.SubmissionStatus;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.AssignmentRepository;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.MarkRepository;
import com.ailearningportal.repository.SubmissionRepository;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.FileStorageService;
import com.ailearningportal.service.SubmissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileStorageService fileStorageService;
    private final MarkRepository markRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public SubmissionServiceImpl(
            AssignmentRepository assignmentRepository,
            EnrollmentRepository enrollmentRepository,
            FileStorageService fileStorageService,
            MarkRepository markRepository,
            SubmissionRepository submissionRepository,
            UserRepository userRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.fileStorageService = fileStorageService;
        this.markRepository = markRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findSubmittedAssignmentIds(String studentEmail) {
        User student = getStudentByEmail(studentEmail);
        return new HashSet<>(submissionRepository.findSubmittedAssignmentIdsByStudentId(student.getId()));
    }

    @Override
    @Transactional
    public Submission submitAssignment(Long assignmentId, SubmissionRequest request, String studentEmail) {
        User student = getStudentByEmail(studentEmail);
        Assignment assignment = assignmentRepository.findByIdWithCourseAndTeacher(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (!assignment.isActive()) {
            throw new IllegalArgumentException("Assignment is not active");
        }

        checkActiveEnrollment(student.getId(), assignment.getCourse().getId());

        if (LocalDate.now().isAfter(assignment.getDueDate())) {
            throw new IllegalArgumentException("Assignment due date is over");
        }

        if (submissionRepository.existsByAssignmentIdAndStudentId(assignment.getId(), student.getId())) {
            throw new IllegalArgumentException("You already submitted this assignment");
        }

        String answerText = cleanText(request.getAnswerText());
        MultipartFile file = request.getFile();
        boolean hasFile = file != null && !file.isEmpty();

        if (answerText == null && !hasFile) {
            throw new IllegalArgumentException("Please write an answer or upload a file");
        }

        Submission submission = new Submission(assignment, student, answerText, SubmissionStatus.SUBMITTED);
        Submission savedSubmission = submissionRepository.save(submission);

        if (hasFile) {
            StoredFile storedFile = fileStorageService.storeSubmissionFile(file, savedSubmission.getId());
            savedSubmission.setFilePath(storedFile.getFilePath());
            savedSubmission.setOriginalFileName(storedFile.getOriginalFileName());
            savedSubmission = submissionRepository.save(savedSubmission);
        }

        return savedSubmission;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> listSubmissionsForAssignment(Long assignmentId, String teacherEmail) {
        getAssignmentForTeacher(assignmentId, teacherEmail);
        return submissionRepository.findByAssignmentIdWithDetails(assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Submission getSubmissionForMark(Long submissionId, String teacherEmail) {
        Submission submission = getSubmissionWithDetails(submissionId);
        User teacher = getTeacherByEmail(teacherEmail);
        checkTeacherOwnsAssignment(submission, teacher);
        return submission;
    }

    @Override
    @Transactional
    public Mark saveMark(Long submissionId, MarkRequest request, String teacherEmail) {
        Submission submission = getSubmissionWithDetails(submissionId);
        User teacher = getTeacherByEmail(teacherEmail);
        checkTeacherOwnsAssignment(submission, teacher);

        if (request.getMarksAwarded() == null || request.getMarksAwarded() < 0) {
            throw new IllegalArgumentException("Marks cannot be negative");
        }

        if (request.getMarksAwarded() > submission.getAssignment().getMaxMarks()) {
            throw new IllegalArgumentException("Marks cannot be greater than assignment max marks");
        }

        Mark mark = markRepository.findBySubmissionId(submission.getId())
                .orElseGet(() -> new Mark(submission, request.getMarksAwarded(), cleanText(request.getFeedback()), teacher));
        mark.setMarksAwarded(request.getMarksAwarded());
        mark.setFeedback(cleanText(request.getFeedback()));
        mark.setGradedByTeacher(teacher);

        Mark savedMark = markRepository.save(mark);
        submission.setMark(savedMark);
        return savedMark;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> listStudentSubmissionsWithMarks(String studentEmail) {
        User student = getStudentByEmail(studentEmail);
        return submissionRepository.findByStudentIdWithDetails(student.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Submission getSubmissionFileForCurrentUser(Long submissionId, String userEmail) {
        Submission submission = getSubmissionWithDetails(submissionId);
        User user = getUserByEmail(userEmail);

        if (user.getRole() == Role.STUDENT && submission.getStudent().getId().equals(user.getId())) {
            return requireFile(submission);
        }

        if (user.getRole() == Role.TEACHER && submission.getAssignment().getTeacher().getId().equals(user.getId())) {
            return requireFile(submission);
        }

        throw new IllegalArgumentException("You cannot download this file");
    }

    private Assignment getAssignmentForTeacher(Long assignmentId, String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        Assignment assignment = assignmentRepository.findByIdWithCourseAndTeacher(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (!assignment.getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("Assignment not found for this teacher");
        }

        return assignment;
    }

    private Submission getSubmissionWithDetails(Long submissionId) {
        return submissionRepository.findByIdWithDetails(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
    }

    private Submission requireFile(Submission submission) {
        if (submission.getFilePath() == null || submission.getFilePath().isBlank()) {
            throw new IllegalArgumentException("Submission file not found");
        }
        return submission;
    }

    private void checkTeacherOwnsAssignment(Submission submission, User teacher) {
        if (!submission.getAssignment().getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("Submission not found for this teacher");
        }
    }

    private void checkActiveEnrollment(Long studentId, Long courseId) {
        boolean enrolled = enrollmentRepository.existsByStudentAndCourseAndStatus(
                studentId,
                courseId,
                EnrollmentStatus.ACTIVE
        );

        if (!enrolled) {
            throw new IllegalArgumentException("You are not enrolled in this course");
        }
    }

    private User getTeacherByEmail(String teacherEmail) {
        User teacher = getUserByEmail(teacherEmail);
        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("Only teacher can manage submissions");
        }
        return teacher;
    }

    private User getStudentByEmail(String studentEmail) {
        User student = getUserByEmail(studentEmail);
        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Only student can submit assignments");
        }
        return student;
    }

    private User getUserByEmail(String email) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User account not found"));
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
