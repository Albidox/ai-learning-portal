package com.ailearningportal.ai;

import com.ailearningportal.dto.AiContentView;
import com.ailearningportal.entity.AiAudienceType;
import com.ailearningportal.entity.AiContentType;
import com.ailearningportal.entity.AiLearningContent;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Lesson;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.AiLearningContentRepository;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.LessonRepository;
import com.ailearningportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LearningContentAiService {

    private final AiLearningContentRepository aiLearningContentRepository;
    private final AiPromptService aiPromptService;
    private final AiResponseValidator aiResponseValidator;
    private final AiUsageService aiUsageService;
    private final EnrollmentRepository enrollmentRepository;
    private final GeminiAiClient geminiAiClient;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    public LearningContentAiService(
            AiLearningContentRepository aiLearningContentRepository,
            AiPromptService aiPromptService,
            AiResponseValidator aiResponseValidator,
            AiUsageService aiUsageService,
            EnrollmentRepository enrollmentRepository,
            GeminiAiClient geminiAiClient,
            LessonRepository lessonRepository,
            UserRepository userRepository
    ) {
        this.aiLearningContentRepository = aiLearningContentRepository;
        this.aiPromptService = aiPromptService;
        this.aiResponseValidator = aiResponseValidator;
        this.aiUsageService = aiUsageService;
        this.enrollmentRepository = enrollmentRepository;
        this.geminiAiClient = geminiAiClient;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Lesson getTeacherLesson(Long lessonId, String teacherEmail) {
        User teacher = getUserByEmailAndRole(teacherEmail, Role.TEACHER);
        return getTeacherOwnedLesson(lessonId, teacher);
    }

    @Transactional(readOnly = true)
    public Lesson getStudentLesson(Long lessonId, String studentEmail) {
        User student = getUserByEmailAndRole(studentEmail, Role.STUDENT);
        Lesson lesson = getPublishedLesson(lessonId);
        checkActiveEnrollment(student, lesson);
        return lesson;
    }

    @Transactional(readOnly = true)
    public AiContentView getTeacherContent(Long lessonId, AiContentType contentType, String teacherEmail) {
        User teacher = getUserByEmailAndRole(teacherEmail, Role.TEACHER);
        getTeacherOwnedLesson(lessonId, teacher);

        return aiLearningContentRepository
                .findTopByLessonIdAndCreatedByUserIdAndContentTypeAndAudienceTypeOrderByCreatedAtDesc(
                        lessonId,
                        teacher.getId(),
                        contentType,
                        AiAudienceType.CLASS_SHARED
                )
                .map(aiResponseValidator::toView)
                .orElseGet(() -> AiContentView.empty(contentType));
    }

    @Transactional(readOnly = true)
    public AiContentView getApprovedClassContentForStudent(
            Long lessonId,
            AiContentType contentType,
            String studentEmail
    ) {
        User student = getUserByEmailAndRole(studentEmail, Role.STUDENT);
        Lesson lesson = getPublishedLesson(lessonId);
        checkActiveEnrollment(student, lesson);

        return aiLearningContentRepository
                .findTopByLessonIdAndContentTypeAndAudienceTypeAndApprovedTrueOrderByCreatedAtDesc(
                        lessonId,
                        contentType,
                        AiAudienceType.CLASS_SHARED
                )
                .map(aiResponseValidator::toView)
                .orElseGet(() -> AiContentView.empty(contentType));
    }

    @Transactional(readOnly = true)
    public AiContentView getStudentPersonalContent(Long lessonId, AiContentType contentType, String studentEmail) {
        User student = getUserByEmailAndRole(studentEmail, Role.STUDENT);
        Lesson lesson = getPublishedLesson(lessonId);
        checkActiveEnrollment(student, lesson);

        return aiLearningContentRepository
                .findTopByLessonIdAndCreatedByUserIdAndContentTypeAndAudienceTypeOrderByCreatedAtDesc(
                        lessonId,
                        student.getId(),
                        contentType,
                        AiAudienceType.PERSONAL
                )
                .map(aiResponseValidator::toView)
                .orElseGet(() -> AiContentView.empty(contentType));
    }

    @Transactional
    public AiLearningContent generateTeacherContent(Long lessonId, AiContentType contentType, String teacherEmail) {
        User teacher = getUserByEmailAndRole(teacherEmail, Role.TEACHER);
        Lesson lesson = getTeacherOwnedLesson(lessonId, teacher);

        Optional<AiLearningContent> savedContent = aiLearningContentRepository
                .findTopByLessonIdAndCreatedByUserIdAndContentTypeAndAudienceTypeOrderByCreatedAtDesc(
                        lessonId,
                        teacher.getId(),
                        contentType,
                        AiAudienceType.CLASS_SHARED
                );

        if (savedContent.isPresent()) {
            return savedContent.get();
        }

        aiUsageService.checkDailyLimit(teacher);
        String prompt = aiPromptService.buildPrompt(lesson, contentType);
        String rawOutput = geminiAiClient.generateJson(prompt);
        String outputJson = aiResponseValidator.validateAndNormalize(contentType, rawOutput);

        AiLearningContent aiContent = new AiLearningContent(
                lesson,
                teacher,
                contentType,
                AiAudienceType.CLASS_SHARED,
                outputJson,
                geminiAiClient.getModelName()
        );

        return aiLearningContentRepository.save(aiContent);
    }

    @Transactional
    public AiLearningContent generateStudentContent(Long lessonId, AiContentType contentType, String studentEmail) {
        User student = getUserByEmailAndRole(studentEmail, Role.STUDENT);
        Lesson lesson = getPublishedLesson(lessonId);
        checkActiveEnrollment(student, lesson);

        Optional<AiLearningContent> savedContent = aiLearningContentRepository
                .findTopByLessonIdAndCreatedByUserIdAndContentTypeAndAudienceTypeOrderByCreatedAtDesc(
                        lessonId,
                        student.getId(),
                        contentType,
                        AiAudienceType.PERSONAL
                );

        if (savedContent.isPresent()) {
            return savedContent.get();
        }

        aiUsageService.checkDailyLimit(student);
        String prompt = aiPromptService.buildPrompt(lesson, contentType);
        String rawOutput = geminiAiClient.generateJson(prompt);
        String outputJson = aiResponseValidator.validateAndNormalize(contentType, rawOutput);

        AiLearningContent aiContent = new AiLearningContent(
                lesson,
                student,
                contentType,
                AiAudienceType.PERSONAL,
                outputJson,
                geminiAiClient.getModelName()
        );

        return aiLearningContentRepository.save(aiContent);
    }

    @Transactional
    public AiLearningContent approveTeacherContent(Long contentId, String teacherEmail) {
        User teacher = getUserByEmailAndRole(teacherEmail, Role.TEACHER);
        AiLearningContent aiContent = aiLearningContentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("AI content not found"));

        if (aiContent.getAudienceType() != AiAudienceType.CLASS_SHARED) {
            throw new IllegalArgumentException("Only class shared AI content can be approved");
        }

        if (!sameId(aiContent.getCreatedByUser().getId(), teacher.getId())) {
            throw new IllegalArgumentException("Teacher can approve only own AI content");
        }

        if (!sameId(aiContent.getLesson().getTeacher().getId(), teacher.getId())) {
            throw new IllegalArgumentException("Teacher can approve AI content only for own lesson");
        }

        aiContent.setApproved(true);
        aiContent.setApprovedByTeacher(teacher);
        aiContent.setApprovedAt(LocalDateTime.now());

        return aiLearningContentRepository.save(aiContent);
    }

    private User getUserByEmailAndRole(String email, Role role) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User account not found"));

        if (user.getRole() != role) {
            throw new IllegalArgumentException("User role is not allowed for this AI action");
        }

        return user;
    }

    private Lesson getTeacherOwnedLesson(Long lessonId, User teacher) {
        return lessonRepository.findByIdAndTeacherId(lessonId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found for this teacher"));
    }

    private Lesson getPublishedLesson(Long lessonId) {
        return lessonRepository.findPublishedLessonById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Published lesson not found"));
    }

    private void checkActiveEnrollment(User student, Lesson lesson) {
        Long courseId = lesson.getSubject().getCourse().getId();
        boolean enrolled = enrollmentRepository.existsByStudentAndCourseAndStatus(
                student.getId(),
                courseId,
                EnrollmentStatus.ACTIVE
        );

        if (!enrolled) {
            throw new IllegalArgumentException("You are not enrolled in this course");
        }
    }

    private boolean sameId(Long first, Long second) {
        return first != null && first.equals(second);
    }
}
