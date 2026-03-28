package com.landgo.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@landgo.com}")
    private String fromEmail;

    @Value("${app.mail.reset-password-url:http://localhost:3000/reset-password}")
    private String resetPasswordBaseUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String userName, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("LandGo - Verify Your Email Address");
            helper.setText(buildVerificationEmailHtml(userName, code), true);
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String userName, String token) {
        try {
            String resetLink = resetPasswordBaseUrl + "?token=" + token;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("LandGo - Password Reset Request");
            helper.setText(buildResetEmailHtml(userName, resetLink), true);
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildVerificationEmailHtml(String userName, String code) {
        return """
                <html><body style="font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0">
                <div style="max-width:600px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,.1)">
                <div style="background:#1B5E20;padding:30px;text-align:center"><h1 style="color:#fff;margin:0">LandGo</h1><p style="color:#C8E6C9;margin:5px 0 0;font-size:14px">Find. Build. Grow.</p></div>
                <div style="padding:40px 30px"><h2 style="color:#333;margin-top:0">Hi %s,</h2>
                <p style="color:#555;line-height:1.6">Welcome to LandGo! Please verify your email address by entering the following code:</p>
                <div style="text-align:center;margin:30px 0"><span style="display:inline-block;background:#E8F5E9;color:#1B5E20;font-size:36px;font-weight:bold;letter-spacing:8px;padding:16px 32px;border-radius:12px;border:2px dashed #4CAF50">%s</span></div>
                <div style="background:#FFF3E0;border-left:4px solid #FF9800;padding:12px 16px;margin:20px 0;border-radius:4px"><strong>⏰ This code expires in 15 minutes.</strong></div>
                </div><div style="background:#f9f9f9;padding:20px 30px;text-align:center;font-size:12px;color:#999"><p>© 2026 LandGo. All rights reserved.</p></div></div></body></html>
                """.formatted(userName, code);
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
}
