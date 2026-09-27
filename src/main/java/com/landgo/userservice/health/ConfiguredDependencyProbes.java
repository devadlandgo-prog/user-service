package com.landgo.userservice.health;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * Probes for dependencies reported as configuration state rather than called live.
 *
 * <p>These are third-party APIs and messaging providers. Calling them on every
 * health check would add load, risk rate limits, and make a vendor's outage read
 * as ours -- and restarting a container cannot fix a vendor anyway, so reporting
 * them as DOWN would be actively misleading.
 */
@Configuration
public class ConfiguredDependencyProbes {

    /** Firebase Cloud Messaging: There is no free no-op send. Delivery cannot be proven without actually messaging a device, so report configuration state instead. */
    @Bean
    public DeepHealthProbe fcmProbe(Environment environment) {
        return new ConfigOnlyProbe("fcm", environment, List.of("firebase.service-account.base64", "firebase.service-account.json", "firebase.service-account.path"));
    }

    /** Google OAuth / identity: A third-party API. Polling it from a health check adds load, risks rate limits, and makes their outage read as ours. */
    @Bean
    public DeepHealthProbe oauthGoogleProbe(Environment environment) {
        return new ConfigOnlyProbe("oauth_google", environment, List.of("app.oauth2.google.additional-audiences", "app.oauth2.google.android-client-id", "app.oauth2.google.client-id", "app.oauth2.google.ios-client-id", "app.oauth2.google.secret-name", "app.oauth2.google.web-client-id"));
    }

    /** SMTP email: Opening an SMTP session per health check is expensive and trips provider abuse heuristics. */
    @Bean
    public DeepHealthProbe smtpProbe(Environment environment) {
        return new ConfigOnlyProbe("smtp", environment, List.of("spring.mail.host", "spring.mail.password", "spring.mail.port", "spring.mail.properties.mail.smtp.auth", "spring.mail.properties.mail.smtp.starttls.enable", "spring.mail.username"));
    }

    /** Twilio messaging / verify: A third-party API. Polling it from a health check adds load, risks rate limits, and makes their outage read as ours. */
    @Bean
    public DeepHealthProbe twilioProbe(Environment environment) {
        return new ConfigOnlyProbe("twilio", environment, List.of("twilio.account-sid", "twilio.auth-token", "twilio.sendgrid.api-key", "twilio.sendgrid.from-email", "twilio.sendgrid.from-name", "twilio.verify-service-sid"));
    }
}
