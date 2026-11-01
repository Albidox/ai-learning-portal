package com.ailearningportal.service.impl;

import com.ailearningportal.dto.RegisterRequest;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerStudent(RegisterRequest request) {
        return registerUser(request, Role.STUDENT);
    }

    @Override
    @Transactional
    public User registerTeacher(RegisterRequest request) {
        return registerUser(request, Role.TEACHER);
    }

    @Override
    @Transactional
    public User createUserIfMissing(String name, String email, String rawPassword, Role role) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> userRepository.save(new User(
                        name.trim(),
                        normalizedEmail,
                        passwordEncoder.encode(rawPassword),
                        role,
                        true
                )));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAllByOrderByRoleAscNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listStudents() {
        return userRepository.findByRoleOrderByNameAsc(Role.STUDENT);
    }

    private void validatePasswordMatch(RegisterRequest request) {
        if (request.getPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }

    private User registerUser(RegisterRequest request, Role role) {
        validatePasswordMatch(request);

        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User(
                request.getName().trim(),
                email,
                passwordEncoder.encode(request.getPassword()),
                role,
                true
        );

        return userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}
