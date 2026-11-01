# Student Setup Guide

This guide uses simple English.

Follow the steps one by one.

## 1. Install required software

Install these:

- Java 21 or newer
- Eclipse IDE or Spring Tool Suite
- PostgreSQL
- DBeaver or pgAdmin
- Maven
- Postman

Postman is optional.

This project mainly runs in the browser.

## 2. Check Java

Open PowerShell or Command Prompt.

Run:

```text
java -version
```

You should see Java 21 or newer.

## 3. Check Maven

Run:

```text
mvn -version
```

You should see Maven using Java 21 or newer.

If Maven uses another Java version, fix Java path first.

## 4. Create PostgreSQL database

Open DBeaver or pgAdmin.

Run this SQL:

```sql
CREATE DATABASE ai_learning_portal;
```

Database name:

```text
ai_learning_portal
```

## 5. Edit local config file

This ZIP already has this file in the project root:

```text
application-local.properties
```

Open it.

It should look like this:

```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/ai_learning_portal
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

app.upload.dir=uploads/assignments
app.sample-data.enabled=true

spring.ai.google.genai.api-key=
spring.ai.google.genai.chat.options.model=gemini-3.5-flash-lite
app.ai.daily-limit=5
app.ai.max-lesson-characters=8000
```

Now change:

```text
your_postgres_password
```

Put your own PostgreSQL password there.

## 6. Gemini API key

Gemini is optional.

For first run, keep:

```properties
spring.ai.google.genai.api-key=
```

Normal learning portal will work.

Only AI notes and quiz generation will show a safe message.

Later, if you have Gemini key, use:

```properties
spring.ai.google.genai.api-key=PASTE_GEMINI_KEY_HERE
```

Do not share your real API key.

Do not upload your real API key to GitHub.

## 7. Import project in Eclipse

Open Eclipse.

Steps:

1. Click `File`.
2. Click `Import`.
3. Select `Existing Maven Projects`.
4. Click `Next`.
5. Select this project folder.
6. Click `Finish`.
7. Wait for Maven dependencies to download.

## 8. Run project in Eclipse

Open this file:

```text
src/main/java/com/ailearningportal/AiLearningPortalApplication.java
```

Right click the file.

Click:

```text
Run As -> Spring Boot App
```

If Spring Boot App option is not visible, click:

```text
Run As -> Java Application
```

Then open browser:

```text
http://localhost:8080
```

## 9. Run project from command line

You can also run from PowerShell:

```text
mvn spring-boot:run
```

Then open browser:

```text
http://localhost:8080
```

## 10. Login accounts

Sample accounts are created automatically.

Admin:

```text
Email: admin@example.test
Password: Password@123
```

Teacher:

```text
Email: teacher@example.test
Password: Password@123
```

Student:

```text
Email: student@example.test
Password: Password@123
```

Student can also register from the `Student Registration` option on the homepage.

Teacher can also register from the `Teacher Registration` option on the homepage.

## 11. Demo flow

Use this simple demo:

1. Start the app.
2. Open `http://localhost:8080`.
3. Login as admin.
4. Open courses.
5. Check sample course `Java Fundamentals`.
6. Open subjects.
7. Check sample subject `Spring Boot Basics`.
8. Open enrollments.
9. Check sample student enrollment.
10. Logout.
11. Login as teacher.
12. Open lessons.
13. Check sample lesson `What Is Spring Boot?`.
14. Open assignments.
15. Check sample assignment.
16. Mark attendance.
17. Generate AI notes or quiz if Gemini key is added.
18. Logout.
19. Login as student.
20. Open courses.
21. Open lesson.
22. Open assignments.
23. Submit text answer.
24. Upload one assignment file.
25. Check marks after teacher gives marks.

## 12. Run tests

Before submission, run:

```text
mvn --no-transfer-progress test
```

Expected result:

```text
BUILD SUCCESS
```

## 13. Create JAR

Run:

```text
mvn --no-transfer-progress package
```

JAR will be created in:

```text
target/
```

## 14. Common problems

### Database error

Check:

- PostgreSQL is running.
- Database `ai_learning_portal` exists.
- Password in `application-local.properties` is correct.
- PostgreSQL port is `5432`.

### Port 8080 already used

Add this in `application-local.properties`:

```properties
server.port=8081
```

Then open:

```text
http://localhost:8081
```

### File upload error

Use only:

```text
PDF, DOCX, TXT, PNG, JPG, JPEG, ZIP, JAVA
```

Maximum file size:

```text
10 MB
```

### Gemini not working

For first demo, Gemini can stay empty.

If you add Gemini key, check:

- API key is correct.
- Internet is working.
- Key is not copied with extra spaces.
- Model name is correct.
