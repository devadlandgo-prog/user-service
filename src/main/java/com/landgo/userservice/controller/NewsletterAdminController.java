package com.landgo.userservice.controller;

import com.landgo.userservice.dto.request.NewsletterPublishRequest;
import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.response.NewsletterCampaignDetailResponse;
import com.landgo.userservice.dto.response.NewsletterCampaignResponse;
import com.landgo.userservice.entity.NewsletterCampaign;
import com.landgo.userservice.enums.CampaignStatus;
import com.landgo.userservice.security.CurrentUser;
import com.landgo.userservice.security.UserPrincipal;
import com.landgo.userservice.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/newsletter")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "newsletter-admin-controller", description = "Compose, send and review newsletter campaigns")
public class NewsletterAdminController {

    private final NewsletterService newsletterService;

    @PostMapping("/publish")
    @Operation(summary = "Publish a newsletter",
            description = "Queues the newsletter for delivery to every active, consented subscriber. "
                    + "htmlBody may be raw HTML or Base64-encoded UTF-8. "
                    + "Returns 202 with status QUEUED once accepted — poll GET /admin/newsletter/campaigns/{id} "
                    + "for the terminal status and delivery counts. "
                    + "Returns 200 with status NO_RECIPIENTS when there is nobody to email, and "
                    + "502 NEWSLETTER_SEND_FAILED when no email provider is configured.")
    public ResponseEntity<ApiResponse<NewsletterCampaignDetailResponse>> publishNewsletter(
            @CurrentUser UserPrincipal adminDetails,
            @Valid @RequestBody NewsletterPublishRequest request) {

        NewsletterCampaign campaign = newsletterService.publish(adminDetails.getId(), request);
        NewsletterCampaignDetailResponse response = newsletterService.getCampaign(campaign.getId());

        // 202 signals the send is still in flight and the admin should poll for the outcome;
        // a terminal NO_RECIPIENTS needs no follow-up, so it stays a plain 200.
        HttpStatus httpStatus = CampaignStatus.QUEUED.equals(campaign.getStatus())
                ? HttpStatus.ACCEPTED
                : HttpStatus.OK;

        return ResponseEntity.status(httpStatus).body(ApiResponse.success(messageFor(campaign), response));
    }

    @GetMapping("/campaigns")
    @Operation(summary = "List published newsletters",
            description = "Paginated send history. Summary fields only — use the detail endpoint for the email body.")
    public ResponseEntity<ApiResponse<Page<NewsletterCampaignResponse>>> getCampaigns(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(newsletterService.getCampaigns(pageable)));
    }

    @GetMapping("/campaigns/{id}")
    @Operation(summary = "Get a newsletter campaign",
            description = "Full campaign including the decoded HTML body that was emailed.")
    public ResponseEntity<ApiResponse<NewsletterCampaignDetailResponse>> getCampaign(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(newsletterService.getCampaign(id)));
    }

    @GetMapping("/subscribers/count")
    @Operation(summary = "Count deliverable subscribers",
            description = "Lets the publish screen warn before sending to an empty list.")
    public ResponseEntity<ApiResponse<Long>> countSubscribers() {
        return ResponseEntity.ok(ApiResponse.success(newsletterService.countActiveSubscribers()));
    }

    /** Gives the admin portal a result string it can surface directly in the publish toast. */
    private String messageFor(NewsletterCampaign campaign) {
        String status = campaign.getStatus();
        if (CampaignStatus.NO_RECIPIENTS.equals(status)) {
            return "Campaign saved but no active subscribers to email";
        }
        if (CampaignStatus.QUEUED.equals(status)) {
            return "Newsletter queued for delivery to " + campaign.getRecipientCount() + " subscriber(s)";
        }
        if (CampaignStatus.PARTIAL.equals(status)) {
            return "Newsletter sent with " + campaign.getFailureCount() + " failure(s)";
        }
        if (CampaignStatus.SENT.equals(status)) {
            return "Newsletter sent";
        }
        return "Newsletter status: " + status;
    }
}
