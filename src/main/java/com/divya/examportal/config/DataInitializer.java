package com.divya.examportal.config;

import com.divya.examportal.model.Exam;
import com.divya.examportal.model.Role;
import com.divya.examportal.model.User;
import com.divya.examportal.repository.ExamRepository;
import com.divya.examportal.repository.UserRepository;
import com.divya.examportal.service.QuestionImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Adds sample data when the app starts with an empty database:
 *  - ready-made exams read from src/main/resources/seed/questions.txt
 *  - one test admin account (admin@exam.com / Admin@123), only when app.seed-test-admin is true.
 *    This is true on your own computer and FALSE in production (see application-prod.properties),
 *    where the real admin is created from the ADMIN_EMAIL and ADMIN_PASSWORD variables instead.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuestionImportService importService;
    private final boolean seedTestAdmin;

    public DataInitializer(ExamRepository examRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           QuestionImportService importService,
                           @Value("${app.seed-test-admin:true}") boolean seedTestAdmin) {
        this.examRepository = examRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.importService = importService;
        this.seedTestAdmin = seedTestAdmin;
    }

    @Override
    public void run(String... args) {
        if (seedTestAdmin) {
            createAdminIfMissing();
        }
        createSampleExamsIfMissing();
    }

    private void createAdminIfMissing() {
        if (userRepository.existsByEmail("admin@exam.com")) {
            return;
        }
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@exam.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);
    }

    private void createSampleExamsIfMissing() {
        if (examRepository.count() > 0) {
            return;
        }

        String content;
        try (InputStream in = new ClassPathResource("seed/questions.txt").getInputStream()) {
            content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Could not read seed/questions.txt, so no sample exams were added: {}", e.getMessage());
            return;
        }

        // Go through the file line by line. An "@exam|..." line starts a new exam.
        // All the lines after it (until the next @exam) are that exam's questions.
        String[] header = null;
        StringBuilder body = new StringBuilder();
        for (String raw : content.split("\\R")) {
            String line = raw.trim();
            if (line.startsWith("@exam|")) {
                saveExam(header, body.toString());
                header = line.split("\\|", -1);
                body.setLength(0);
            } else {
                body.append(line).append("\n");
            }
        }
        saveExam(header, body.toString());
    }

    /** header = {"@exam", title, description, minutes, questionsPerStudent} */
    private void saveExam(String[] header, String questionLines) {
        if (header == null) {
            return; // text before the first @exam line, like the comments at the top
        }
        if (header.length < 4) {
            log.warn("Skipping a bad @exam line in seed/questions.txt: needs title, description and minutes");
            return;
        }

        Exam exam = new Exam();
        exam.setTitle(header[1].trim());
        exam.setDescription(header[2].trim());
        exam.setDurationMinutes(Integer.parseInt(header[3].trim()));
        if (header.length > 4 && !header[4].isBlank()) {
            exam.setQuestionsPerAttempt(Integer.valueOf(header[4].trim()));
        }

        QuestionImportService.ParseResult parsed = importService.parse(questionLines);
        for (String problem : parsed.errors()) {
            log.warn("seed/questions.txt, exam \"{}\": {}", exam.getTitle(), problem);
        }
        parsed.questions().forEach(exam::addQuestion);

        examRepository.save(exam);
    }
}
