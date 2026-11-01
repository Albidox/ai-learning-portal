package com.ailearningportal.service;

import com.ailearningportal.dto.RegisterRequest;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.User;

import java.util.List;

public interface UserService {

    User registerStudent(RegisterRequest request);

    User registerTeacher(RegisterRequest request);

    User createUserIfMissing(String name, String email, String rawPassword, Role role);

    List<User> listUsers();

    List<User> listStudents();
}
