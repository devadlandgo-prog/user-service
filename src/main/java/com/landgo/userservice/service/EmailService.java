package com.landgo.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    @Value("${app.mail.from:noreply@landgo.ca}")
    private String fromEmail;

    @Value("${app.mail.reset-password-url:http://localhost:3000/reset-password}")
    private String resetPasswordBaseUrl;

    @Value("${app.mail.verify-link-url:http://localhost:3000/verify-email}")
    private String verifyLinkBaseUrl;

    @Value("${app.mail.logo-url:https://landgo.app/logo_with_tagline.png}")
    private String logoUrl;

    @Value("${app.mail.verification-template:email-templates/verification-email.html}")
    private String verificationTemplatePath;

    @Value("${twilio.sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${twilio.sendgrid.from-email:noreply@landgo.ca}")
    private String sendGridFromEmail;

    @Value("${twilio.sendgrid.from-name:LandGo}")
    private String sendGridFromName;

    @Async
    public void sendVerificationEmail(String toEmail, String userName, String code, String verificationToken) {
        try {
            String verificationUrl = verifyLinkBaseUrl + "?token=" + verificationToken;
            java.util.Map<String, String> vars = new java.util.HashMap<>();
            vars.put("User", userName);
            vars.put("verificationCode", code);
            vars.put("verificationUrl", verificationUrl);
            sendTemplateEmail(toEmail, "LandGo - Verify Your Email Address", "EmailVerification", vars);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String userName, String token) {
        try {
            String resetLink = resetPasswordBaseUrl + "?token=" + token;
            java.util.Map<String, String> vars = new java.util.HashMap<>();
            vars.put("User", userName);
            vars.put("verificationCode", token);
            vars.put("resetUrl", resetLink);
            sendTemplateEmail(toEmail, "LandGo - Password Reset Request", "ForgotPassword", vars);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Async
    public void sendPasswordResetCodeEmail(String toEmail, String userName, String code) {
        try {
            java.util.Map<String, String> vars = new java.util.HashMap<>();
            vars.put("User", userName);
            vars.put("verificationCode", code);
            vars.put("resetUrl", resetPasswordBaseUrl + "?code=" + code);
            sendTemplateEmail(toEmail, "LandGo - Password Reset Verification Code", "ForgotPassword", vars);
            log.info("Password reset code email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset code email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset code email", e);
        }
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            java.util.Map<String, String> vars = new java.util.HashMap<>();
            vars.put("User", userName);
            sendTemplateEmail(toEmail, "Welcome to LandGo!", "WelcomeEmail", vars);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendSubscriptionExpiryEmail(String toEmail, String userName, int daysLeft, String planCategory) {
        try {
            String subject = daysLeft == 1 
                ? "ACTION REQUIRED: Your LandGo subscription expires tomorrow" 
                : "Reminder: Your LandGo subscription expires in " + daysLeft + " days";
            java.util.Map<String, String> vars = new java.util.HashMap<>();
            vars.put("User", userName);
            vars.put("planName", planCategory);
            vars.put("daysLeft", String.valueOf(daysLeft));
            sendTemplateEmail(toEmail, subject, "SubscriptionExpiring", vars);
            log.info("Subscription expiry warning email sent to: {} ({} days left)", toEmail, daysLeft);
        } catch (Exception e) {
            log.error("Failed to send subscription expiry warning email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendDynamicHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            sendHtmlEmail(toEmail, subject, htmlContent);
            log.info("Dynamic email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send dynamic email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send dynamic email", e);
        }
    }

    @Async
    public void sendTemplateEmail(String toEmail, String subject, String templateName, Map<String, String> variables) {
        try {
            String html = buildTemplateEmailHtml(templateName, variables);
            sendHtmlEmail(toEmail, subject, html);
        } catch (Exception e) {
            log.error("Failed to send template email '{}' to: {}", templateName, toEmail, e);
            throw new RuntimeException("Failed to send template email", e);
        }
    }

    private String buildTemplateEmailHtml(String templateName, Map<String, String> variables) throws IOException {
        String templatePath = "email-templates/" + templateName + ".html";
        ClassPathResource resource = new ClassPathResource(templatePath);
        if (!resource.exists()) {
            throw new IllegalArgumentException("Template file not found: " + templatePath);
        }
        String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Inject logoUrl
        template = template.replace("/static/icon.svg", logoUrl);
        template = template.replace("{{logoUrl}}", logoUrl);

        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() != null ? entry.getValue() : "";
                template = template.replace("<!-- -->" + key + "<!-- -->", value);
                template = template.replace("{{" + key + "}}", value);
                template = template.replace("${" + key + "}", value);
            }
        }
        return template;
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            if (sendGridApiKey != null && !sendGridApiKey.isBlank()) {
                log.debug("Attempting to send email via SendGrid to: {}", to);
                sendViaSendGrid(to, subject, htmlContent);
                log.info("Email sent successfully via SendGrid to: {}", to);
            } else {
                log.debug("SendGrid API key not found, falling back to JavaMailSender for: {}", to);
                sendViaJavaMail(to, subject, htmlContent);
                log.info("Email sent successfully via JavaMailSender to: {}", to);
            }
        } catch (Exception e) {
            log.error("CRITICAL: Failed to send email to {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private void sendViaJavaMail(String toEmail, String subject, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    private void sendViaSendGrid(String toEmail, String subject, String html) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(sendGridApiKey);

            Map<String, Object> payload = Map.of(
                    "personalizations", List.of(Map.of("to", List.of(Map.of("email", toEmail)))),
                    "from", Map.of(
                            "email", sendGridFromEmail == null || sendGridFromEmail.isBlank() ? fromEmail : sendGridFromEmail,
                            "name", sendGridFromName
                    ),
                    "subject", subject,
                    "content", List.of(Map.of("type", "text/html", "value", html))
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.sendgrid.com/v3/mail/send",
                    new HttpEntity<>(payload, headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Twilio SendGrid returned non-success status: " + response.getStatusCode());
            }
        } catch (RestClientException ex) {
            throw new RuntimeException("Failed to send email via Twilio SendGrid", ex);
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
