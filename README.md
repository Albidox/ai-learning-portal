# AI Learning Portal

This is a Java 21 Spring Boot student project.

It is a monolithic web application. That means frontend and backend are in the same Spring Boot project.

Frontend is made using:

- Thymeleaf HTML pages;
- Bootstrap CSS from CDN;
- small local CSS file.

Backend is made using:

- Spring Boot;
- Spring MVC;
- Spring Security;
- Spring Data JPA;
- PostgreSQL;
- Gemini API for AI notes and quiz generation.

## Main features

- Student registration and login
- Admin course creation
- Admin subject creation
- Admin student enrollment
- Teacher lesson creation
- Teacher lesson publishing
- Attendance
- Assignments
- Text assignment submission
- File upload submission
- Marks and feedback
- AI notes and quiz generation

## First file to read

Please read:

```text
SETUP-GUIDE-STUDENTS.md
```

That file explains how to run this project in simple steps.

## Important safety note

This ZIP includes `application-local.properties` with placeholder values.

This ZIP does not include:

- real database password
- real Gemini API key
- uploaded assignment files
- generated `target/` folder
- agent kit files
- planning folders
- screenshots

You must change your local database password before running the project.
