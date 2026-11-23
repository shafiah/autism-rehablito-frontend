
package com.autismcentre.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminNotificationRecipientResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.autismcentre.model.NotificationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminNotificationDetailsActivity
        extends BaseAdminActivity {

    private TextView tvAdminNotificationDetailTitle;
    private TextView tvAdminNotificationDetailMessage;
    private TextView tvAdminNotificationDetailType;
    private TextView tvAdminNotificationDetailRecipient;
    private TextView tvAdminNotificationDetailBranch;
    private TextView tvAdminNotificationDetailDate;
    private TextView tvAdminNotificationDetailStatus;

    private TextView tvAdminNotificationReadState;
    private TextView tvAdminNoCampaignRecipients;

    private LinearLayout adminNotificationRecipientsContainer;

    private MaterialButton btnAdminNotificationDetailsBack;
    private MaterialButton btnAdminNotificationStatusAction;

    private ApiService apiService;

    private Long campaignId;

    private String campaignStatus;

    private Call<List<AdminNotificationRecipientResponse>>
            recipientsCall;

    private Call<Void> cancelCampaignCall;

    private boolean isReceivedNotification = false;

    private Call<NotificationResponse> receivedDetailsCall;

    private Call<com.google.gson.JsonObject> markReceivedReadCall;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_notification_details
        );

        ApiClient.initialize(this);

        apiService = ApiClient.getApiService();

        initializeAdminNotificationDetailsViews();

        setupAdminNotificationDetailsBackButton();

        isReceivedNotification = getIntent().getBooleanExtra(
                "is_received_notification",
                false
        );

        if (isReceivedNotification) {

            loadReceivedNotificationDetails();

        } else {

            loadAdminNotificationDetails();

            loadCampaignRecipients();
        }
    }

    private void initializeAdminNotificationDetailsViews() {

        tvAdminNotificationDetailTitle =
                findViewById(
                        R.id.tvAdminNotificationDetailTitle
                );

        tvAdminNotificationDetailMessage =
                findViewById(
                        R.id.tvAdminNotificationDetailMessage
                );

        tvAdminNotificationDetailType =
                findViewById(
                        R.id.tvAdminNotificationDetailType
                );

        tvAdminNotificationDetailRecipient =
                findViewById(
                        R.id.tvAdminNotificationDetailRecipient
                );

        tvAdminNotificationDetailBranch =
                findViewById(
                        R.id.tvAdminNotificationDetailBranch
                );

        tvAdminNotificationDetailDate =
                findViewById(
                        R.id.tvAdminNotificationDetailDate
                );

        tvAdminNotificationDetailStatus =
                findViewById(
                        R.id.tvAdminNotificationDetailStatus
                );

        tvAdminNotificationReadState =
                findViewById(
                        R.id.tvAdminNotificationReadState
                );

        btnAdminNotificationDetailsBack =
                findViewById(
                        R.id.btnAdminNotificationDetailsBack
                );

        btnAdminNotificationStatusAction =
                findViewById(
                        R.id.btnAdminNotificationStatusAction
                );

        adminNotificationRecipientsContainer =
                findViewById(
                        R.id.adminNotificationRecipientsContainer
                );

        tvAdminNoCampaignRecipients =
                findViewById(
                        R.id.tvAdminNoCampaignRecipients
                );
    }

    private void loadReceivedNotificationDetails() {

        Long notificationId = getIntent().getLongExtra(
                "notification_id",
                -1L
        );

        if (notificationId <= 0) {
            Toast.makeText(
                    this,
                    "Invalid notification ID",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        // Campaign recipients are not applicable here.
        View recipientsCard =
                (View) adminNotificationRecipientsContainer
                        .getParent()
                        .getParent();

        recipientsCard.setVisibility(View.GONE);

        btnAdminNotificationStatusAction.setVisibility(View.GONE);

        receivedDetailsCall =
                apiService.getAdminNotificationById(notificationId);

        receivedDetailsCall.enqueue(
                new Callback<NotificationResponse>() {

                    @Override
                    public void onResponse(
                            Call<NotificationResponse> call,
                            Response<NotificationResponse> response
                    ) {

                        if (call.isCanceled()) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Toast.makeText(
                                    AdminNotificationDetailsActivity.this,
                                    "Unable to load notification details. Code: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        NotificationResponse notification =
                                response.body();

                        displayReceivedNotification(notification);

                        if (!Boolean.TRUE.equals(
                                notification.getIsRead())) {

                            markReceivedNotificationAsRead(
                                    notificationId
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<NotificationResponse> call,
                            Throwable t
                    ) {

                        if (call.isCanceled()) {
                            return;
                        }

                        Toast.makeText(
                                AdminNotificationDetailsActivity.this,
                                "Notification loading failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void displayReceivedNotification(
            NotificationResponse notification
    ) {

        tvAdminNotificationDetailTitle.setText(
                notification.getTitle()
        );

        tvAdminNotificationDetailMessage.setText(
                notification.getMessage()
        );

        tvAdminNotificationDetailType.setText(
                notification.getType()
        );

        tvAdminNotificationDetailRecipient.setText("Admin");

        Long branchId = notification.getBranchId();

        tvAdminNotificationDetailBranch.setText(
                branchId != null
                        ? "Branch #" + branchId
                        : "Unknown"
        );

        String createdAt = notification.getCreatedAt();

        tvAdminNotificationDetailDate.setText(
                createdAt != null
                        ? createdAt.replace("T", " ")
                        : "Unknown"
        );

        boolean isRead = Boolean.TRUE.equals(
                notification.getIsRead()
        );

        String status = isRead ? "Read" : "Unread";

        tvAdminNotificationDetailStatus.setText(status);

        tvAdminNotificationReadState.setText(
                "Notification Status: " + status
        );

        tvAdminNotificationReadState.setTypeface(
                null,
                isRead ? Typeface.NORMAL : Typeface.BOLD
        );
    }

    private void markReceivedNotificationAsRead(
            Long notificationId
    ) {

        markReceivedReadCall =
                apiService.markAdminNotificationAsRead(
                        notificationId
                );

        markReceivedReadCall.enqueue(
                new Callback<com.google.gson.JsonObject>() {

                    @Override
                    public void onResponse(
                            Call<com.google.gson.JsonObject> call,
                            Response<com.google.gson.JsonObject> response
                    ) {

                        if (call.isCanceled()) {
                            return;
                        }

                        if (response.isSuccessful()) {

                            tvAdminNotificationDetailStatus.setText(
                                    "Read"
                            );

                            tvAdminNotificationReadState.setText(
                                    "Notification Status: Read"
                            );

                            setResult(RESULT_OK);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<com.google.gson.JsonObject> call,
                            Throwable t
                    ) {

                        // Details remain visible even if read update fails.
                    }
                }
        );
    }



    // ==========================================
    // Load Campaign Information
    // ==========================================

    private void loadAdminNotificationDetails() {

        Intent intent = getIntent();

        campaignId = intent.getLongExtra(
                "campaign_id",
                -1L
        );

        if (campaignId <= 0) {
            campaignId = null;
        }

        String title =
                intent.getStringExtra("notification_title");

        String message =
                intent.getStringExtra("notification_message");

        String type =
                intent.getStringExtra("notification_type");

        String recipient =
                intent.getStringExtra("notification_recipient");

        String branch =
                intent.getStringExtra("notification_branch");

        String date =
                intent.getStringExtra("notification_date");

        campaignStatus =
                intent.getStringExtra("notification_status");

        if (title == null) {
            title = "Notification";
        }

        if (message == null) {
            message = "No message available.";
        }

        if (type == null) {
            type = "General";
        }

        if (recipient == null) {
            recipient = "Unknown";
        }

        if (branch == null) {
            branch = "Unknown";
        }

        if (date == null) {
            date = "Unknown";
        }

        if (campaignStatus == null) {
            campaignStatus = "Unknown";
        }

        tvAdminNotificationDetailTitle.setText(title);

        tvAdminNotificationDetailMessage.setText(message);

        tvAdminNotificationDetailType.setText(type);

        tvAdminNotificationDetailRecipient.setText(recipient);

        tvAdminNotificationDetailBranch.setText(branch);

        tvAdminNotificationDetailDate.setText(date);

        tvAdminNotificationDetailStatus.setText(campaignStatus);

        int total = intent.getIntExtra(
                "notification_total_recipients",
                0
        );

        int read = intent.getIntExtra(
                "notification_read_count",
                0
        );

        int unread = intent.getIntExtra(
                "notification_unread_count",
                0
        );

        tvAdminNotificationReadState.setText(
                "Total: " + total
                        + " | Read: " + read
                        + " | Unread: " + unread
        );

        tvAdminNotificationReadState.setTypeface(
                null,
                unread > 0
                        ? Typeface.BOLD
                        : Typeface.NORMAL
        );

        setupAdminNotificationStatusAction();
    }

    // ==========================================
    // Load Campaign Recipients
    // ==========================================

    private void loadCampaignRecipients() {

        if (campaignId == null) {

            showNoRecipients(
                    "Campaign ID is unavailable."
            );

            return;
        }

        if (recipientsCall != null) {
            recipientsCall.cancel();
        }

        recipientsCall =
                apiService.getAdminNotificationCampaignRecipients(
                        campaignId
                );

        recipientsCall.enqueue(
                new Callback<List<AdminNotificationRecipientResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminNotificationRecipientResponse>> call,
                            Response<List<AdminNotificationRecipientResponse>> response
                    ) {

                        if (call.isCanceled()) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            showNoRecipients(
                                    "Unable to load recipients. Code: "
                                            + response.code()
                            );

                            return;
                        }

                        displayCampaignRecipients(
                                response.body()
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminNotificationRecipientResponse>> call,
                            Throwable t
                    ) {

                        if (call.isCanceled()) {
                            return;
                        }

                        showNoRecipients(
                                "Recipient loading failed: "
                                        + t.getMessage()
                        );
                    }
                }
        );
    }

    private void displayCampaignRecipients(
            List<AdminNotificationRecipientResponse> recipients
    ) {

        adminNotificationRecipientsContainer.removeAllViews();

        if (recipients == null || recipients.isEmpty()) {

            showNoRecipients(
                    "No recipients found for this campaign."
            );

            return;
        }

        tvAdminNoCampaignRecipients.setVisibility(View.GONE);

        for (AdminNotificationRecipientResponse recipient
                : recipients) {

            addCampaignRecipientCard(recipient);
        }
    }

    private void addCampaignRecipientCard(
            AdminNotificationRecipientResponse recipient
    ) {

        MaterialCardView card =
                new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dpToPx(10);

        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(10));
        card.setCardElevation(dpToPx(2));
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(
                getResources().getColor(
                        android.R.color.darker_gray
                )
        );

        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dpToPx(14),
                dpToPx(12),
                dpToPx(14),
                dpToPx(12)
        );

        String name = recipient.getRecipientName();

        if (name == null || name.trim().isEmpty()) {
            name = "Unknown Recipient";
        }

        String role = recipient.getRole();

        if (role == null) {
            role = "Unknown";
        }

        String branch = recipient.getBranchName();

        if (branch == null || branch.trim().isEmpty()) {
            branch = "Unknown";
        }

        boolean isRead = Boolean.TRUE.equals(
                recipient.getIsRead()
        );

        TextView tvName = createRecipientText(
                name,
                16,
                true
        );

        TextView tvRole = createRecipientText(
                "Role: " + role,
                14,
                false
        );

        TextView tvBranch = createRecipientText(
                "Branch: " + branch,
                14,
                false
        );

        TextView tvReadStatus = createRecipientText(
                isRead ? "Status: Read" : "Status: Unread",
                14,
                true
        );

        tvReadStatus.setTextColor(
                getResources().getColor(
                        isRead
                                ? R.color.primary
                                : android.R.color.holo_red_dark
                )
        );

        content.addView(tvName);
        content.addView(tvRole);
        content.addView(tvBranch);
        content.addView(tvReadStatus);

        card.addView(content);

        adminNotificationRecipientsContainer.addView(card);
    }

    private TextView createRecipientText(
            String value,
            int textSize,
            boolean bold
    ) {

        TextView textView = new TextView(this);

        textView.setText(value);
        textView.setTextSize(textSize);
        textView.setTextColor(
                getResources().getColor(
                        android.R.color.primary_text_light
                )
        );

        if (bold) {
            textView.setTypeface(
                    null,
                    Typeface.BOLD
            );
        }

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dpToPx(5);

        textView.setLayoutParams(params);

        return textView;
    }

    private void showNoRecipients(String message) {

        adminNotificationRecipientsContainer.removeAllViews();

        tvAdminNoCampaignRecipients.setText(message);

        tvAdminNoCampaignRecipients.setVisibility(
                View.VISIBLE
        );
    }

    private int dpToPx(int dp) {

        return (int) (
                dp * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // ==========================================
    // Scheduled Campaign Cancellation
    // ==========================================

    private void setupAdminNotificationStatusAction() {

        btnAdminNotificationStatusAction.setVisibility(
                View.GONE
        );

        if ("Scheduled".equalsIgnoreCase(campaignStatus)) {

            btnAdminNotificationStatusAction.setVisibility(
                    View.VISIBLE
            );

            btnAdminNotificationStatusAction.setText(
                    "Cancel Schedule"
            );

            btnAdminNotificationStatusAction.setOnClickListener(
                    v -> showCancelConfirmation()
            );
        }
    }

    private void showCancelConfirmation() {

        new AlertDialog.Builder(this)
                .setTitle("Cancel Schedule")
                .setMessage(
                        "Are you sure you want to cancel this scheduled notification?"
                )
                .setNegativeButton(
                        "No",
                        null
                )
                .setPositiveButton(
                        "Yes",
                        (dialog, which) -> cancelScheduledCampaign()
                )
                .show();
    }

    private void cancelScheduledCampaign() {

        if (campaignId == null) {

            Toast.makeText(
                    this,
                    "Campaign ID is unavailable.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnAdminNotificationStatusAction.setEnabled(false);

        cancelCampaignCall =
                apiService.cancelAdminScheduledNotification(
                        campaignId
                );

        cancelCampaignCall.enqueue(
                new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (call.isCanceled()) {
                            return;
                        }

                        btnAdminNotificationStatusAction.setEnabled(true);

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    AdminNotificationDetailsActivity.this,
                                    "Scheduled notification cancelled successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent resultIntent = new Intent();

                            resultIntent.putExtra(
                                    "campaign_status_updated",
                                    true
                            );

                            setResult(
                                    RESULT_OK,
                                    resultIntent
                            );

                            finish();

                        } else {

                            Toast.makeText(
                                    AdminNotificationDetailsActivity.this,
                                    "Unable to cancel schedule. Code: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        if (call.isCanceled()) {
                            return;
                        }

                        btnAdminNotificationStatusAction.setEnabled(true);

                        Toast.makeText(
                                AdminNotificationDetailsActivity.this,
                                "Cancellation failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // ==========================================
    // Back Button
    // ==========================================

    private void setupAdminNotificationDetailsBackButton() {

        btnAdminNotificationDetailsBack.setOnClickListener(
                v -> finish()
        );
    }

    @Override
    protected void onDestroy() {

        if (recipientsCall != null) {
            recipientsCall.cancel();
        }

        if (cancelCampaignCall != null) {
            cancelCampaignCall.cancel();
        }

        if (receivedDetailsCall != null) {
            receivedDetailsCall.cancel();
        }

        if (markReceivedReadCall != null) {
            markReceivedReadCall.cancel();
        }

        super.onDestroy();
    }
}
