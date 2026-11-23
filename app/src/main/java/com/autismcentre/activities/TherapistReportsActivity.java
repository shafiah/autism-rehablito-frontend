
package com.autismcentre.activities;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.TherapistReportResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistReportsActivity extends BaseTherapistActivity {

    private TextView tvTotalReports;
    private TextView tvSubmittedReports;
    private TextView tvPendingReports;
    private TextView tvReportsMessage;

    private ProgressBar progressReportsLoading;
    private LinearLayout reportsContainer;
    private MaterialButton btnRefreshReports;

    private ApiService apiService;
    private Call<List<TherapistReportResponse>> reportsCall;

    private final List<TherapistReportResponse> reportList =
            new ArrayList<>();

    private static final String PURPLE = "#061B49";
    private static final String DARK_TEXT = "#29243A";
    private static final String SECONDARY_TEXT = "#6F6A7A";
   // private static final String BACKGROUND = "#F8F7FC";
    private static final String BORDER = "#E5E0EF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageTitle("Rehablito Autism Center");
        showPageContent(R.layout.activity_therapist_reports);

        apiService = ApiClient.getApiService();

        tvTotalReports = findViewById(R.id.tvTotalReports);
        tvSubmittedReports = findViewById(R.id.tvSubmittedReports);
        tvPendingReports = findViewById(R.id.tvPendingReports);
        tvReportsMessage = findViewById(R.id.tvReportsMessage);

        progressReportsLoading =
                findViewById(R.id.progressReportsLoading);

        reportsContainer = findViewById(R.id.reportsContainer);
        btnRefreshReports = findViewById(R.id.btnRefreshReports);

        btnRefreshReports.setOnClickListener(v -> loadReports());

        loadReports();
    }

    private void loadReports() {

        if (reportsCall != null) {
            reportsCall.cancel();
        }

        progressReportsLoading.setVisibility(View.VISIBLE);
        btnRefreshReports.setEnabled(false);

        tvReportsMessage.setText("Loading reports...");
        tvReportsMessage.setVisibility(View.VISIBLE);

        reportsContainer.removeAllViews();

        reportsCall = apiService.getMyTherapistReports();

        reportsCall.enqueue(new Callback<List<TherapistReportResponse>>() {

            @Override
            public void onResponse(
                    Call<List<TherapistReportResponse>> call,
                    Response<List<TherapistReportResponse>> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                progressReportsLoading.setVisibility(View.GONE);
                btnRefreshReports.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {

                    reportList.clear();
                    reportList.addAll(response.body());

                    updateOverview();
                    displayReports();

                } else {

                    reportList.clear();
                    updateOverview();

                    tvReportsMessage.setText(
                            "Unable to load reports. HTTP error: "
                                    + response.code()
                                    + "\nPlease refresh and try again."
                    );

                    tvReportsMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(
                    Call<List<TherapistReportResponse>> call,
                    Throwable throwable) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                progressReportsLoading.setVisibility(View.GONE);
                btnRefreshReports.setEnabled(true);

                if (call.isCanceled()) {
                    return;
                }

                tvReportsMessage.setText(
                        "Network error while loading reports.\n"
                                + "Check your internet connection and refresh."
                );

                tvReportsMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateOverview() {

        int total = reportList.size();
        int submitted = 0;
        int drafts = 0;

        for (TherapistReportResponse report : reportList) {

            String status = report.getStatus();

            if (status == null) {
                continue;
            }

            if ("SUBMITTED".equalsIgnoreCase(status)) {
                submitted++;
            } else if ("DRAFT".equalsIgnoreCase(status)) {
                drafts++;
            }
        }

        tvTotalReports.setText("Total Reports: " + total);
        tvSubmittedReports.setText("Submitted Reports: " + submitted);
        tvPendingReports.setText("Draft Reports: " + drafts);
    }

    private void displayReports() {

        reportsContainer.removeAllViews();

        if (reportList.isEmpty()) {

            tvReportsMessage.setText(
                    "No reports found yet.\n"
                            + "Create a report from a child's report screen."
            );

            tvReportsMessage.setVisibility(View.VISIBLE);
            return;
        }

        tvReportsMessage.setVisibility(View.GONE);

        for (TherapistReportResponse report : reportList) {
            reportsContainer.addView(createReportCard(report));
        }
    }

    private MaterialCardView createReportCard(
            TherapistReportResponse report) {

        MaterialCardView card = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dp(14);

        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(16));
        card.setCardElevation(dp(2));
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(Color.parseColor(BORDER));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(16));

        card.addView(content);

        TextView tvChildName = createTextView(
                "Child: " + safeText(report.getChildName(), "Unknown"),
                17,
                DARK_TEXT,
                true
        );

        content.addView(tvChildName);

        addSpace(content, 8);

        TextView tvTitle = createTextView(
                safeText(report.getReportTitle(), "Untitled Report"),
                15,
                PURPLE,
                true
        );

        content.addView(tvTitle);

        addSpace(content, 8);

        String status = safeText(report.getStatus(), "UNKNOWN");

        TextView tvStatus = new TextView(this);
        tvStatus.setText("Status: " + status);
        tvStatus.setTextSize(13);
        tvStatus.setTextColor(getStatusColor(status));
        tvStatus.setTypeface(null, Typeface.BOLD);
        content.addView(tvStatus);
       // content.addView(tvStatus);

        addSpace(content, 6);

        String createdAt = report.getCreatedAt();

        TextView tvDate = createTextView(
                "Created: " + safeText(createdAt, "Not available"),
                12,
                SECONDARY_TEXT,
                false
        );

        content.addView(tvDate);

        addSpace(content, 12);

        MaterialButton btnViewDetails = new MaterialButton(this);

        btnViewDetails.setText("View Details");
        btnViewDetails.setTextColor(Color.WHITE);
        btnViewDetails.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.parseColor(PURPLE)
                )
        );

        content.addView(
                btnViewDetails,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        btnViewDetails.setOnClickListener(
                v -> showReportDetails(report)
        );

        card.setOnClickListener(v -> showReportDetails(report));

        return card;
    }

    private void showReportDetails(TherapistReportResponse report) {

        StringBuilder details = new StringBuilder();

        details.append("Child: ")
                .append(safeText(report.getChildName(), "Unknown"))
                .append("\n\n");

        details.append("Report Title: ")
                .append(safeText(report.getReportTitle(), "Untitled"))
                .append("\n\n");

        details.append("Status: ")
                .append(safeText(report.getStatus(), "Unknown"))
                .append("\n\n");

        details.append("Report Content:\n")
                .append(safeText(
                        report.getReportContent(),
                        "No report content available."
                ));

        if (report.getCreatedAt() != null) {
            details.append("\n\nCreated: ")
                    .append(report.getCreatedAt());
        }

        if (report.getSubmittedAt() != null) {
            details.append("\n\nSubmitted: ")
                    .append(report.getSubmittedAt());
        }

        if (report.getAdminRemarks() != null
                && !report.getAdminRemarks().trim().isEmpty()) {

            details.append("\n\nAdmin Remarks:\n")
                    .append(report.getAdminRemarks());
        }

        TextView detailsText = createTextView(
                details.toString(),
                14,
                DARK_TEXT,
                false
        );

        detailsText.setPadding(dp(4), dp(4), dp(4), dp(4));
        detailsText.setTextIsSelectable(true);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        scrollView.addView(detailsText);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Report Details")
                .setView(scrollView)
                .setPositiveButton("Close", null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.white
            );
        }
    }

    private TextView createTextView(
            String text,
            int textSize,
            String textColor,
            boolean bold) {

        TextView textView = new TextView(this);

        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setTextColor(Color.parseColor(textColor));
        textView.setGravity(Gravity.START);

        if (bold) {
            textView.setTypeface(null, Typeface.BOLD);
        }

        return textView;
    }

    private int getStatusColor(String status) {

        if ("SUBMITTED".equalsIgnoreCase(status)) {
            return Color.parseColor("#287D45");
        }

        if ("DRAFT".equalsIgnoreCase(status)) {
            return Color.parseColor("#B36B00");
        }

        if ("PUBLISHED".equalsIgnoreCase(status)) {
            return Color.parseColor("#287D45");
        }

        return Color.parseColor(SECONDARY_TEXT);
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    private void addSpace(LinearLayout layout, int heightDp) {

        View space = new View(this);

        layout.addView(
                space,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(heightDp)
                )
        );
    }

    private int dp(int value) {
        return (int) (value
                * getResources().getDisplayMetrics().density
                + 0.5f);
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.therapist_nav_reports;
    }

    @Override
    protected void onDestroy() {

        if (reportsCall != null) {
            reportsCall.cancel();
        }

        super.onDestroy();
    }
}
