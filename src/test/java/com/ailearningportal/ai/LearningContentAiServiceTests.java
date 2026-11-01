package com.ailearningportal.ai;

import com.ailearningportal.entity.AiAudienceType;
import com.ailearningportal.entity.AiContentType;
import com.ailearningportal.entity.AiLearningContent;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Lesson;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.Subject;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.AiLearningContentRepository;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.LessonRepository;
import com.ailearningportal.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LearningContentAiServiceTests {

    @Test
    void teacherGenerationShouldReuseSavedOutputBeforeCallingGemini() {
        TestContext context = context();
        User teacher = teacher();
        Lesson lesson = lesson(teacher);
        AiLearningContent savedContent = content(lesson, teacher, AiAudienceType.CLASS_SHARED);

        when(context.userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher));
        when(context.lessonRepository.findByIdAndTeacherId(50L, 1L)).thenReturn(Optional.of(lesson));
        when(context.aiLearningContentRepository
                .findTopByLessonIdAndCreatedByUserIdAndContentTypeAndAudienceTypeOrderByCreatedAtDesc(
                        50L,
                        1L,
                        AiContentType.SUMMARY,
                        AiAudienceType.CLASS_SHARED
                ))
                .thenReturn(Optional.of(savedContent));

        AiLearningContent result = context.service.generateTeacherContent(
                50L,
                AiContentType.SUMMARY,
                "teacher@example.test"
        );

        assertSame(savedContent, result);
        verify(context.aiUsageService, never()).checkDailyLimit(any());
        verify(context.geminiAiClient, never()).generateJson(any());
    }

    @Test
    void teacherGenerationShouldSaveValidatedOutput() {
        TestContext context = context();
        User teacher = teacher();
        Lesson lesson = lesson(teacher);

        when(context.userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher));
        when(context.lessonRepository.findByIdAndTeacherId(50L, 1L)).thenReturn(Optional.of(lesson));
        when(context.aiLearningContentRepository
                .findTopByLessonIdAndCreatedByUserIdAndContentTypeAndAudienceTypeOrderByCreatedAtDesc(
                        50L,
                        1L,
                        AiContentType.SUMMARY,
                        AiAudienceType.CLASS_SHARED
                ))
                .thenReturn(Optional.empty());
        when(context.aiPromptService.buildPrompt(lesson, AiContentType.SUMMARY)).thenReturn("prompt");
        when(context.geminiAiClient.generateJson("prompt")).thenReturn("{raw}");
        when(context.aiResponseValidator.validateAndNormalize(AiContentType.SUMMARY, "{raw}"))
                .thenReturn("{\"summary\":\"ok\",\"importantPoints\":[\"p\"],\"keyTerms\":[\"k\"]}");
        when(context.geminiAiClient.getModelName()).thenReturn("gemini-test");
        when(context.aiLearningContentRepository.save(any(AiLearningContent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiLearningContent result = context.service.generateTeacherContent(
                50L,
                AiContentType.SUMMARY,
                "teacher@example.test"
        );

        assertEquals(AiAudienceType.CLASS_SHARED, result.getAudienceType());
        assertEquals("gemini-test", result.getModelName());
    }

    @Test
    void studentGenerationShouldBlockStudentWithoutEnrollment() {
        TestContext context = context();
        User student = student();
        Lesson lesson = lesson(teacher());

        when(context.userRepository.findByEmail("student@example.test")).thenReturn(Optional.of(student));
        when(context.lessonRepository.findPublishedLessonById(50L)).thenReturn(Optional.of(lesson));
        when(context.enrollmentRepository.existsByStudentAndCourseAndStatus(2L, 10L, EnrollmentStatus.ACTIVE))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> context.service.generateStudentContent(50L, AiContentType.QUIZ, "student@example.test")
        );

        verify(context.aiLearningContentRepository, never()).save(any());
    }

    @Test
    void teacherCannotApproveStudentPersonalContent() {
        TestContext context = context();
        User teacher = teacher();
        AiLearningContent personalContent = content(lesson(teacher), student(), AiAudienceType.PERSONAL);

        when(context.userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher));
        when(context.aiLearningContentRepository.findById(9L)).thenReturn(Optional.of(personalContent));

        assertThrows(
                IllegalArgumentException.class,
                () -> context.service.approveTeacherContent(9L, "teacher@example.test")
        );
    }

    private TestContext context() {
        AiLearningContentRepository aiLearningContentRepository = mock(AiLearningContentRepository.class);
        AiPromptService aiPromptService = mock(AiPromptService.class);
        AiResponseValidator aiResponseValidator = mock(AiResponseValidator.class);
        AiUsageService aiUsageService = mock(AiUsageService.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        GeminiAiClient geminiAiClient = mock(GeminiAiClient.class);
        LessonRepository lessonRepository = mock(LessonRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        LearningContentAiService service = new LearningContentAiService(
                aiLearningContentRepository,
                aiPromptService,
                aiResponseValidator,
                aiUsageService,
                enrollmentRepository,
                geminiAiClient,
                lessonRepository,
                userRepository
        );

        return new TestContext(
                service,
                aiLearningContentRepository,
                aiPromptService,
                aiResponseValidator,
                aiUsageService,
                enrollmentRepository,
                geminiAiClient,
                lessonRepository,
                userRepository
        );
    }

    private AiLearningContent content(Lesson lesson, User user, AiAudienceType audienceType) {
        AiLearningContent content = new AiLearningContent();
        content.setId(9L);
        content.setLesson(lesson);
        content.setCreatedByUser(user);
        content.setContentType(AiContentType.SUMMARY);
        content.setAudienceType(audienceType);
        content.setOutputJson("{\"summary\":\"ok\",\"importantPoints\":[\"p\"],\"keyTerms\":[\"k\"]}");
        content.setModelName("gemini-test");
        return content;
    }

    private Lesson lesson(User teacher) {
        Course course = new Course();
        course.setId(10L);
        course.setActive(true);

        Subject subject = new Subject();
        subject.setCourse(course);

        Lesson lesson = new Lesson();
        lesson.setId(50L);
        lesson.setSubject(subject);
        lesson.setTeacher(teacher);
        lesson.setTitle("Spring Boot MVC");
        lesson.setContent("Controller reads request and returns page.");
        lesson.setPublished(true);
        return lesson;
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
            LearningContentAiService service,
            AiLearningContentRepository aiLearningContentRepository,
            AiPromptService aiPromptService,
            AiResponseValidator aiResponseValidator,
            AiUsageService aiUsageService,
            EnrollmentRepository enrollmentRepository,
            GeminiAiClient geminiAiClient,
            LessonRepository lessonRepository,
            UserRepository userRepository
    ) {
    }
}
