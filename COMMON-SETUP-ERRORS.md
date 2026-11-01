# Common Setup Errors

This guide explains common student setup problems.

Use it when the project does not run.

## 1. Java Version Is Wrong

Problem:

```text
java -version
```

does not show Java 21.

Fix:

Install Java 21.

Then set `JAVA_HOME` to Java 21.

Check again:

```powershell
java -version
```

## 2. Maven Uses Wrong Java

Problem:

```text
mvn -version
```

does not show Java 21.

Fix:

Make sure Maven uses the same Java 21 installation.

Close PowerShell and open it again.

Then run:

```powershell
mvn -version
```

## 3. Database Does Not Exist

Problem:

App says database connection failed.

Possible reason:

Database was not created.

Fix:

Create this database in PostgreSQL:

```text
ai_learning_portal
```

Do not change spelling.

## 4. PostgreSQL Password Is Wrong

Problem:

App console shows authentication failed.

Meaning:

The PostgreSQL password in `application-local.properties` is wrong.

Fix:

Open:

```text
application-local.properties
```

Set your real password:

```properties
spring.datasource.password=your_postgres_password
```

Then run:

```powershell
mvn spring-boot:run
```

## 5. PostgreSQL Is Not Running

Problem:

App cannot connect to localhost port 5432.

Fix:

Start PostgreSQL service.

Then try again.

## 6. Port 8080 Already In Use

Problem:

App says port 8080 is already in use.

Meaning:

Another app is already running on port 8080.

Fix:

Open:

```text
application-local.properties
```

Change:

```properties
server.port=8081
```

Open:

```text
http://localhost:8081
```

## 7. Maven Dependencies Are Not Downloading

Problem:

Eclipse shows many errors.

Possible reasons:

- internet is slow
- Maven download is not finished
- Maven cache has a problem

Fix in Eclipse:

1. Right-click project.
2. Click `Maven`.
3. Click `Update Project`.
4. Select the project.
5. Click `Force Update of Snapshots/Releases`.
6. Click `OK`.

Fix in PowerShell:

```powershell
mvn -U clean compile
```

## 8. Eclipse Does Not Show Spring Boot App

Problem:

`Run As > Spring Boot App` is missing.

Fix:

Use:

```text
Run As > Java Application
```

Run this class:

```text
AiLearningPortalApplication
```

## 9. Login Does Not Work

Problem:

Sample login fails.

Check:

```text
admin@example.test
teacher@example.test
student@example.test
```

Password:

```text
Password@123
```

Also check:

- sample data is enabled
- app started without database error
- you are using the correct database

## 10. Sample Data Is Missing

Problem:

Sample course or users are not visible.

Check:

Sample data setting should be true:

```properties
app.sample-data.enabled=true
```

Default value is true.

Restart the app.

## 11. Gemini AI Does Not Work

Problem:

AI generation shows an error.

Possible reasons:

- Gemini API key is missing
- API key is wrong
- Gemini quota is over
- internet is not working
- model name is wrong

Fix:

Open:

```text
application-local.properties
```

Set API key:

```properties
spring.ai.google.genai.api-key=your_gemini_api_key
```

Restart the app.

Important:

Do not write the key in source code.

## 12. File Upload Does Not Work

Problem:

Student cannot upload assignment file.

Check allowed file types:

```text
pdf
docx
txt
png
jpg
jpeg
zip
java
```

Max file size:

```text
10MB
```

Upload folder:

```text
uploads/assignments
```

## 13. Page Shows Access Denied

Problem:

You see access denied.

Meaning:

You are logged in with the wrong role.

Example:

- admin cannot open teacher pages
- teacher cannot open student pages
- student cannot open admin pages

Fix:

Logout.

Login with the correct sample user.

## 14. Best Debug Order

When there is a problem, check in this order:

1. Java 21
2. Maven Java version
3. PostgreSQL is running
4. database exists
5. database password in `application-local.properties` is correct
6. app starts
7. browser URL is correct
8. sample user email and password are correct
9. role is correct
10. Gemini key only if AI fails
