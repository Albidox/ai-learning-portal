# Setup Verification Checklist

Use this checklist after setup.

Mark each item as pass or fail.

## 1. Software Check

### Java

Command:

```powershell
java -version
```

Pass:

Java 21 shows.

### Maven

Command:

```powershell
mvn -version
```

Pass:

Maven shows Java 21.

### PostgreSQL

Pass:

PostgreSQL is installed and running.

### Database

Pass:

Database exists:

```text
ai_learning_portal
```

## 2. Local Settings File Check

File:

```text
application-local.properties
```

Required:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ai_learning_portal
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password
```

Optional:

```properties
spring.ai.google.genai.api-key=
spring.ai.google.genai.chat.options.model=gemini-3.5-flash-lite
app.sample-data.enabled=true
```

Pass:

Database values are correct.

The password is changed to your real PostgreSQL password.

## 3. Compile Check

Command:

```powershell
mvn -DskipTests compile
```

Pass:

Build says success.

## 4. Run Check

Command:

```powershell
mvn spring-boot:run
```

Pass:

Console shows Spring Boot started.

## 5. Browser Check

Open:

```text
http://localhost:8080
```

Pass:

Home page opens.

If you changed port:

```text
http://localhost:8081
```

## 6. Login Check

Login as admin:

```text
admin@example.test
Password@123
```

Pass:

Admin dashboard opens.

Login as teacher:

```text
teacher@example.test
Password@123
```

Pass:

Teacher dashboard opens.

Login as student:

```text
student@example.test
Password@123
```

Pass:

Student dashboard opens.

## 7. Sample Data Check

Admin check:

- course list shows `Java Fundamentals`
- subject list shows `Spring Boot Basics`
- enrollment list has sample student

Teacher check:

- lessons page shows `What Is Spring Boot?`
- assignments page shows `Write Short Notes About Spring Boot`

Student check:

- courses page shows `Java Fundamentals`
- lesson page opens
- assignment page shows sample assignment

## 8. AI Check

Without Gemini API key:

Pass:

Normal portal works.

AI generation can show friendly error.

With Gemini API key:

Pass:

Teacher or student can generate AI notes or quiz.

## 9. Postman Basic Check

Optional:

```text
GET http://localhost:8080/
GET http://localhost:8080/login
GET http://localhost:8080/register
```

Pass:

Pages return HTML.

Use browser for real login testing.

## 10. What To Send For Help

If setup fails, send:

1. Screenshot of error.
2. Output of `java -version`.
3. Output of `mvn -version`.
4. Last 30 lines from Spring Boot console.
5. Tell if you are running from Eclipse or PowerShell.
6. Tell your PostgreSQL database name.

Do not send your real Gemini API key.

Do not send your real PostgreSQL password.
