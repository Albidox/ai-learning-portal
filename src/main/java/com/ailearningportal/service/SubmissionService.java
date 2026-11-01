package com.ailearningportal.service;

import com.ailearningportal.dto.MarkRequest;
import com.ailearningportal.dto.SubmissionRequest;
import com.ailearningportal.entity.Mark;
import com.ailearningportal.entity.Submission;

import java.util.List;
import java.util.Set;

public interface SubmissionService {

    Set<Long> findSubmittedAssignmentIds(String studentEmail);

    Submission submitAssignment(Long assignmentId, SubmissionRequest request, String studentEmail);

    List<Submission> listSubmissionsForAssignment(Long assignmentId, String teacherEmail);

    Submission getSubmissionForMark(Long submissionId, String teacherEmail);

    Mark saveMark(Long submissionId, MarkRequest request, String teacherEmail);

    List<Submission> listStudentSubmissionsWithMarks(String studentEmail);

    Submission getSubmissionFileForCurrentUser(Long submissionId, String userEmail);
}
