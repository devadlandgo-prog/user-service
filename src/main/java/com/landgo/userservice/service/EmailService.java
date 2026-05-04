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

    @Value("${app.mail.from:noreply@landgo.com}")
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

    @Value("${twilio.sendgrid.from-email:noreply@landgo.com}")
    private String sendGridFromEmail;

    @Value("${twilio.sendgrid.from-name:LandGo}")
    private String sendGridFromName;

    @Async
    public void sendVerificationEmail(String toEmail, String userName, String code, String verificationToken) {
        try {
            String verificationUrl = verifyLinkBaseUrl + "?token=" + verificationToken;
            String html = buildVerificationEmailHtml(userName, code, verificationUrl);
            sendHtmlEmail(toEmail, "LandGo - Verify Your Email Address", html);
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
            sendHtmlEmail(toEmail, "LandGo - Password Reset Request", buildResetEmailHtml(userName, resetLink));
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildVerificationEmailHtml(String userName, String code, String verificationUrl) throws IOException {
        ClassPathResource resource = new ClassPathResource(verificationTemplatePath);
        String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return template
                .replace("{{logoUrl}}", logoUrl)
                .replace("{{userName}}", escapeHtml(userName))
                .replace("{{verificationCode}}", escapeHtml(code))
                .replace("{{expiryMinutes}}", "15")
                .replace("{{verificationUrl}}", verificationUrl);
    }

    private String buildResetEmailHtml(String userName, String resetLink) {
        return """
                <html><body style="font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0">
                <div style="max-width:600px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,.1)">
                <div style="background:#1B5E20;padding:30px;text-align:center"><h1 style="color:#fff;margin:0">LandGo</h1><p style="color:#C8E6C9;margin:5px 0 0;font-size:14px">Find. Build. Grow.</p></div>
                <div style="padding:40px 30px"><h2 style="color:#333;margin-top:0">Hi %s,</h2>
                <p style="color:#555;line-height:1.6">We received a request to reset your password. Click the button below:</p>
                <p style="text-align:center"><a href="%s" style="display:inline-block;background:#1B5E20;color:#fff;text-decoration:none;padding:14px 40px;border-radius:8px;font-size:16px;font-weight:bold;margin:20px 0">Reset Password</a></p>
                <div style="background:#FFF3E0;border-left:4px solid #FF9800;padding:12px 16px;margin:20px 0;border-radius:4px"><strong>⏰ This link expires in 30 minutes.</strong></div>
                <p style="word-break:break-all;font-size:12px;color:#888">%s</p>
                </div><div style="background:#f9f9f9;padding:20px 30px;text-align:center;font-size:12px;color:#999"><p>© 2026 LandGo. All rights reserved.</p></div></div></body></html>
                """.formatted(userName, resetLink, resetLink);
    }

    private void sendHtmlEmail(String toEmail, String subject, String html) throws MessagingException {
        if (sendGridApiKey != null && !sendGridApiKey.isBlank()) {
            sendViaSendGrid(toEmail, subject, html);
            return;
        }
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
