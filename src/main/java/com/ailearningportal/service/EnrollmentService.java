package com.ailearningportal.service;

import com.ailearningportal.dto.EnrollmentRequest;
import com.ailearningportal.entity.Enrollment;

import java.util.List;

public interface EnrollmentService {

    List<Enrollment> listEnrollments();

    Enrollment createEnrollment(EnrollmentRequest request);
}
