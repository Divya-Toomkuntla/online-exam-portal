# Online Exam Portal

A full stack web app where admins create timed exams and students take them, get an instant score with a review, and see their past results.

**Live demo:** _add your deployed link here_

## Features
- Register and login with Spring Security and JWT
- Two roles: **Admin** and **Student** (registration always creates a student)
- Admin: create exams, add and delete multiple choice questions, choose how many random questions each student gets, reset attempts, delete exams
- Student: see available exams, take a timed exam (auto-submits when time is up), see score and answer review, view past results
- Bulk import: the admin pastes or uploads many questions at once, one per line:
  `Question | Option A | Option B | Option C | Option D | Correct letter | Marks (optional)`
  (all-or-nothing: if any line has a problem, nothing is added and the bad lines are listed)
- Four ready-made exams (Java, Spring Boot and REST, SQL, Web basics, 65 questions) are loaded from `src/main/resources/seed/questions.txt` on local start
- One attempt per student per exam, enforced on the server (database unique constraint)
- Each student gets a random set of questions from the pool, in random order, with shuffled options
- The timer is also checked on the server: answers that arrive after the time (plus a small allowance) are not counted
- Correct answers are never sent to the browser until the exam is submitted

## Tech stack
- Java 21, Spring Boot 3 (Web, Data JPA, Validation, Security)
- JWT (jjwt), BCrypt password hashing
- Hibernate / JPA, MySQL (H2 for local development)
- HTML, CSS, JavaScript front end
- JUnit 5 and MockMvc tests
- Docker

## Run locally
```bash
mvn spring-boot:run
```
Open http://localhost:8080. It uses an in-memory H2 database, so no MySQL is needed (data resets when you stop the app).

- Test admin (local only): `admin@exam.com` / `Admin@123`
- Database viewer: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:examdb`, user `sa`, empty password)

## Run the tests
```bash
mvn test
```

## API
| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public (returns a JWT) |
| GET | `/api/me` | Logged in |
| GET | `/api/exams` | Logged in: exams with questions |
| POST | `/api/exams/{id}/start` | Logged in: starts (or resumes) the one attempt, returns the shuffled paper |
| POST | `/api/exams/{id}/submit` | Logged in: marks answers, saves result |
| GET | `/api/results` | Logged in: my past results |
| GET | `/api/admin/exams` | Admin: list exams |
| POST | `/api/admin/exams` | Admin: create exam |
| GET | `/api/admin/exams/{id}` | Admin: exam with questions and answers |
| DELETE | `/api/admin/exams/{id}` | Admin: delete exam |
| DELETE | `/api/admin/exams/{id}/attempts` | Admin: reset all attempts and results of an exam |
| POST | `/api/admin/exams/{id}/questions` | Admin: add question |
| POST | `/api/admin/exams/{id}/questions/import` | Admin: add many questions from pasted text |
| DELETE | `/api/admin/exams/{examId}/questions/{questionId}` | Admin: delete question |

Send the token with each request: `Authorization: Bearer <token>`.

## Deploy (Render + a hosted MySQL)
1. Push this project to GitHub.
2. Create a free MySQL database (for example on Aiven). Note the host, port, database name, user and password.
3. On Render, create a **Web Service** from the GitHub repo with the **Docker** runtime.
4. Add these environment variables:

| Variable | Example |
|---|---|
| `DB_URL` | `jdbc:mysql://HOST:PORT/DATABASE?ssl-mode=REQUIRED` |
| `DB_USERNAME` | your database user |
| `DB_PASSWORD` | your database password |
| `JWT_SECRET` | a long random text, 32+ characters |
| `ADMIN_EMAIL` | the email you will use as admin |
| `ADMIN_PASSWORD` | a strong admin password |

5. Deploy. Open the live link, log in with `ADMIN_EMAIL` / `ADMIN_PASSWORD`, and create your first exam.
6. Paste the live link at the top of this README.

## Known limits and ideas
- Answers are not saved while the student is still answering. If the browser closes mid-exam, the student can resume, but unsaved choices are lost.
- Possible next steps: edit questions, a React front end.
