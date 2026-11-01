package com.ailearningportal.service;

import com.ailearningportal.dto.AssignmentRequest;
import com.ailearningportal.entity.Assignment;

import java.util.List;

public interface AssignmentService {

    List<Assignment> listAssignmentsForTeacher(String teacherEmail);

    Assignment createAssignment(AssignmentRequest request, String teacherEmail);

    Assignment getAssignmentForTeacher(String teacherEmail, Long assignmentId);

    List<Assignment> listAssignmentsForStudent(String studentEmail);

    Assignment getActiveAssignmentForStudent(String studentEmail, Long assignmentId);
}
