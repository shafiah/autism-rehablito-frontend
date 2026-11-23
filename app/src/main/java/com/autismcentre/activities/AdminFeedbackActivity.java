
package com.autismcentre.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminFeedbackResponse;
import com.google.android.material.card.MaterialCardView;

import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AlertDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFeedbackActivity extends BaseAdminActivity {

    private static final String TAG = "AdminFeedbackActivity";

    private ApiService apiService;

    private TextView tvAdminFeedbackMessage;
    private ProgressBar progressAdminFeedback;
    private LinearLayout layoutAdminFeedbackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Feedback Management");

        showAdminPageContent(
                R.layout.activity_admin_feedback
        );

        apiService = ApiClient.getApiService();

        tvAdminFeedbackMessage =
                findViewById(R.id.tvAdminFeedbackMessage);

        progressAdminFeedback =
                findViewById(R.id.progressAdminFeedback);

        layoutAdminFeedbackList =
                findViewById(R.id.layoutAdminFeedbackList);

        loadAdminFeedback();
    }

    // =====================================================
    // LOAD ADMIN FEEDBACK
    // =====================================================

    private void loadAdminFeedback() {

        progressAdminFeedback.setVisibility(View.VISIBLE);
        tvAdminFeedbackMessage.setVisibility(View.GONE);

        apiService.getAdminFeedback().enqueue(
                new Callback<List<AdminFeedbackResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminFeedbackResponse>> call,
                            Response<List<AdminFeedbackResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        progressAdminFeedback.setVisibility(View.GONE);

                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<AdminFeedbackResponse> feedbackList =
                                    response.body();

                            layoutAdminFeedbackList.removeAllViews();

                            if (feedbackList.isEmpty()) {

                                showFeedbackMessage(
                                        "No parent feedback available yet."
                                );

                                return;
                            }

                            tvAdminFeedbackMessage.setVisibility(View.GONE);

                            for (AdminFeedbackResponse feedback : feedbackList) {

                                if (feedback != null) {
                                    addFeedbackCard(feedback);
                                }
                            }

                            Log.d(
                                    TAG,
                                    "Admin feedback loaded: "
                                            + feedbackList.size()
                            );

                        } else {

                            Log.e(
                                    TAG,
                                    "Feedback API failed. HTTP: "
                                            + response.code()
                            );

                            showFeedbackMessage(
                                    "Unable to load feedback. HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminFeedbackResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        progressAdminFeedback.setVisibility(View.GONE);

                        Log.e(TAG, "Feedback API request failed", t);

                        showFeedbackMessage(
                                "Network error. Please try again."
                        );
                    }
                }
        );
    }

    // =====================================================
    // ADD FEEDBACK CARD
    // =====================================================

    private void addFeedbackCard(AdminFeedbackResponse feedback) {

        MaterialCardView card = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, dp(10), 0, dp(10));

        card.setLayoutParams(cardParams);
        card.setRadius(dp(16));
        card.setCardElevation(dp(2));
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(
                ContextCompat.getColor(this, R.color.card_border)
        );
        card.setCardBackgroundColor(
                ContextCompat.getColor(
                        this,
                        R.color.parent_card_background
                )
        );

        LinearLayout content = new LinearLayout(this);

        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        card.addView(content);

        addText(
                content,
                "Parent: " + safeValue(feedback.getParentName()),
                17,
                R.color.parent_navy,
                true
        );

        addText(
                content,
                "Child: " + safeValue(feedback.getChildName()),
                15,
                android.R.color.black,
                false
        );

        addText(
                content,
                "Therapist: " + safeValue(feedback.getTherapistName()),
                15,
                android.R.color.black,
                false
        );

        int rating = feedback.getRating() != null
                ? feedback.getRating() : 0;

        addText(
                content,
                "Rating: " + rating + "/5  "
                        + getStars(rating),
                16,
                R.color.parent_navy,
                true
        );

        addText(
                content,
                safeValue(feedback.getMessage()),
                15,
                android.R.color.black,
                false
        );

        addText(
                content,
                "Submitted: " + formatDate(feedback.getCreatedAt()),
                13,
                android.R.color.darker_gray,
                false
        );

        String status = safeValue(feedback.getStatus());

        addText(
                content,
                "Status: " + status,
                14,
                "PUBLISHED".equalsIgnoreCase(status)
                        ? android.R.color.holo_green_dark
                        : android.R.color.holo_orange_dark,
                true
        );

        // Publish button only for SUBMITTED feedback
        if ("SUBMITTED".equalsIgnoreCase(status)) {

            MaterialButton btnPublish = new MaterialButton(this);

            btnPublish.setText("Publish Feedback");
            btnPublish.setAllCaps(false);
            btnPublish.setCornerRadius(dp(10));

            LinearLayout.LayoutParams buttonParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            buttonParams.setMargins(0, dp(12), 0, 0);

            btnPublish.setLayoutParams(buttonParams);

            content.addView(btnPublish);

            btnPublish.setOnClickListener(v -> {

                showPublishConfirmation(
                        feedback.getFeedbackId(),
                        btnPublish
                );
            });
        }

        layoutAdminFeedbackList.addView(card);
    }


    private void showPublishConfirmation(
            Long feedbackId,
            MaterialButton btnPublish) {

        new AlertDialog.Builder(this)
                .setTitle("Publish Feedback")
                .setMessage(
                        "Are you sure you want to publish this feedback?"
                )
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Publish", (dialog, which) -> {

                    publishFeedback(feedbackId, btnPublish);

                })
                .show();
    }

// =====================================================
// PUBLISH FEEDBACK API
// =====================================================

    private void publishFeedback(
            Long feedbackId,
            MaterialButton btnPublish) {

        if (feedbackId == null || feedbackId <= 0) {
            Toast.makeText(
                    this,
                    "Invalid feedback ID",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnPublish.setEnabled(false);
        btnPublish.setText("Publishing...");

        apiService.publishAdminFeedback(feedbackId).enqueue(
                new Callback<AdminFeedbackResponse>() {

                    @Override
                    public void onResponse(
                            Call<AdminFeedbackResponse> call,
                            Response<AdminFeedbackResponse> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            Toast.makeText(
                                    AdminFeedbackActivity.this,
                                    "Feedback published successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Reload records from backend.
                            loadAdminFeedback();

                        } else {

                            btnPublish.setEnabled(true);
                            btnPublish.setText("Publish Feedback");

                            Toast.makeText(
                                    AdminFeedbackActivity.this,
                                    "Unable to publish feedback. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminFeedbackResponse> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        btnPublish.setEnabled(true);
                        btnPublish.setText("Publish Feedback");

                        Log.e(TAG, "Publish feedback failed", t);

                        Toast.makeText(
                                AdminFeedbackActivity.this,
                                "Network error. Please try again.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    // =====================================================
    // ADD TEXT TO CARD
    // =====================================================

    private void addText(
            LinearLayout parent,
            String value,
            int textSize,
            int textColor,
            boolean bold) {

        TextView textView = new TextView(this);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, dp(6), 0, dp(6));

        textView.setLayoutParams(params);
        textView.setText(value);
        textView.setTextSize(textSize);
        textView.setTextColor(
                ContextCompat.getColor(this, textColor)
        );

        if (bold) {
            textView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        parent.addView(textView);
    }

    // =====================================================
    // FORMAT FEEDBACK DATE - ALREADY IST
    // =====================================================

    private String formatDate(String dateValue) {

        if (dateValue == null || dateValue.trim().isEmpty()) {
            return "Not available";
        }

        try {

            String timestamp = dateValue.trim();

            if (timestamp.length() >= 19) {
                timestamp = timestamp.substring(0, 19);
            }

            SimpleDateFormat inputFormat =
                    new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.US
                    );

            inputFormat.setLenient(false);
            inputFormat.setTimeZone(
                    TimeZone.getTimeZone("Asia/Kolkata")
            );

            Date date = inputFormat.parse(timestamp);

            if (date == null) {
                return "Not available";
            }

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.US
                    );

            outputFormat.setTimeZone(
                    TimeZone.getTimeZone("Asia/Kolkata")
            );

            return outputFormat.format(date);

        } catch (ParseException e) {

            Log.e(TAG, "Feedback date parsing failed", e);

            return "Not available";
        }
    }

    // =====================================================
    // STAR DISPLAY
    // =====================================================

    private String getStars(int rating) {

        StringBuilder stars = new StringBuilder();

        for (int i = 1; i <= 5; i++) {
            stars.append(i <= rating ? "★" : "☆");
        }

        return stars.toString();
    }

    // =====================================================
    // SAFE STRING
    // =====================================================

    private String safeValue(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "Not available";
        }

        return value.trim();
    }

    // =====================================================
    // SHOW MESSAGE
    // =====================================================

    private void showFeedbackMessage(String message) {

        layoutAdminFeedbackList.removeAllViews();

        tvAdminFeedbackMessage.setText(message);
        tvAdminFeedbackMessage.setVisibility(View.VISIBLE);
    }

    // =====================================================
    // DP CONVERSION
    // =====================================================

    private int dp(int value) {

        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
