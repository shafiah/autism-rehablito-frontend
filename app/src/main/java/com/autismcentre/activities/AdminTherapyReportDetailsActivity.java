
package com.autismcentre.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminReportResponse;
import com.autismcentre.model.AdminReportReviewRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminTherapyReportDetailsActivity extends BaseAdminActivity {

    private TextView tvAdminReportDetailChild;
    private TextView tvAdminReportDetailTherapist;
    private TextView tvAdminReportDetailBranch;
    private TextView tvAdminReportDetailTherapy;
    private TextView tvAdminReportDetailSessionDate;
    private TextView tvAdminReportDetailReportDate;
    private TextView tvAdminReportDetailStatus;
    private TextView tvAdminReportDetailSummary;
    private TextView tvAdminReportDetailObservations;
    private TextView tvAdminReportDetailProgress;
    private TextView tvAdminReportDetailRemarks;

    private MaterialButton btnAdminReportChangeStatus;
    private MaterialButton btnAdminReportBack;

    private ApiService apiService;
    private Long reportId;
    private String reportStatus = "";
    private boolean requestInProgress = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");
        showAdminPageContent(
                R.layout.activity_admin_therapy_report_details
        );

        apiService = ApiClient.getApiService();

        initializeAdminReportDetailViews();
        setupAdminReportBackButton();

        reportId = getIntent().hasExtra("report_id")
                ? getIntent().getLongExtra("report_id", -1L)
                : -1L;

        if (reportId == null || reportId <= 0) {
            Toast.makeText(
                    this,
                    "Report ID is missing. Please open a report from the report list.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        loadReportFromIntent();
        loadAdminReportFromBackend();
        setupAdminReportStatusButton();
    }

    private void initializeAdminReportDetailViews() {
        tvAdminReportDetailChild = findViewById(R.id.tvAdminReportDetailChild);
        tvAdminReportDetailTherapist = findViewById(R.id.tvAdminReportDetailTherapist);
        tvAdminReportDetailBranch = findViewById(R.id.tvAdminReportDetailBranch);
        tvAdminReportDetailTherapy = findViewById(R.id.tvAdminReportDetailTherapy);
        tvAdminReportDetailSessionDate = findViewById(R.id.tvAdminReportDetailSessionDate);
        tvAdminReportDetailReportDate = findViewById(R.id.tvAdminReportDetailReportDate);
        tvAdminReportDetailStatus = findViewById(R.id.tvAdminReportDetailStatus);
        tvAdminReportDetailSummary = findViewById(R.id.tvAdminReportDetailSummary);
        tvAdminReportDetailObservations = findViewById(R.id.tvAdminReportDetailObservations);
        tvAdminReportDetailProgress = findViewById(R.id.tvAdminReportDetailProgress);
        tvAdminReportDetailRemarks = findViewById(R.id.tvAdminReportDetailRemarks);
        btnAdminReportChangeStatus = findViewById(R.id.btnAdminReportChangeStatus);
        btnAdminReportBack = findViewById(R.id.btnAdminReportBack);
    }

    private void loadReportFromIntent() {
        Intent intent = getIntent();

        setText(tvAdminReportDetailChild, intent.getStringExtra("report_child"));
        setText(tvAdminReportDetailTherapist, intent.getStringExtra("report_therapist"));
        setText(tvAdminReportDetailBranch, intent.getStringExtra("report_branch"));
        setText(tvAdminReportDetailTherapy, intent.getStringExtra("report_therapy"));
        setText(tvAdminReportDetailSessionDate, intent.getStringExtra("report_session_date"));
        setText(tvAdminReportDetailReportDate, intent.getStringExtra("report_date"));
        setText(tvAdminReportDetailSummary, intent.getStringExtra("report_summary"));
        setText(tvAdminReportDetailObservations, intent.getStringExtra("report_observations"));
        setText(tvAdminReportDetailProgress, intent.getStringExtra("report_progress"));
        setText(tvAdminReportDetailRemarks, intent.getStringExtra("report_remarks"));

        reportStatus = normalizeStatus(intent.getStringExtra("report_status"));
        updateStatusUI();
    }

    private void loadAdminReportFromBackend() {
        // The report list already supplied the report data.
        // The detail screen uses that data and updates it from successful
        // Review/Publish API responses, avoiding a nonexistent detail endpoint.
        updateStatusUI();
    }

    private void setText(TextView view, String value) {
        view.setText(
                value == null || value.trim().isEmpty()
                        ? "Not available"
                        : value
        );
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "UNKNOWN";
        }
        return status.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void updateStatusUI() {
        String displayStatus = reportStatus.isEmpty()
                ? "UNKNOWN"
                : reportStatus.substring(0, 1)
                + reportStatus.substring(1).toLowerCase(java.util.Locale.ROOT);

        tvAdminReportDetailStatus.setText(displayStatus);

        if ("PUBLISHED".equals(reportStatus)) {
            tvAdminReportDetailStatus.setTextColor(Color.parseColor("#2E7D32"));
            btnAdminReportChangeStatus.setText("Published");
            btnAdminReportChangeStatus.setEnabled(false);
        } else if ("REVIEWED".equals(reportStatus)) {
            tvAdminReportDetailStatus.setTextColor(Color.parseColor("#1565C0"));
            btnAdminReportChangeStatus.setText("Publish Report");
            btnAdminReportChangeStatus.setEnabled(!requestInProgress);
        } else if ("SUBMITTED".equals(reportStatus)) {
            tvAdminReportDetailStatus.setTextColor(Color.parseColor("#F57C00"));
            btnAdminReportChangeStatus.setText("Review Report");
            btnAdminReportChangeStatus.setEnabled(!requestInProgress);
        } else {
            tvAdminReportDetailStatus.setTextColor(Color.GRAY);
            btnAdminReportChangeStatus.setText("Action unavailable");
            btnAdminReportChangeStatus.setEnabled(false);
        }
    }

    private void setupAdminReportStatusButton() {
        btnAdminReportChangeStatus.setOnClickListener(v -> {
            if (requestInProgress) {
                return;
            }

            if ("SUBMITTED".equals(reportStatus)) {
                showReviewReportDialog();
            } else if ("REVIEWED".equals(reportStatus)) {
                showPublishReportDialog();
            } else if ("PUBLISHED".equals(reportStatus)) {
                Snackbar.make(
                        btnAdminReportChangeStatus,
                        "This report is already published.",
                        Snackbar.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showReviewReportDialog() {
        EditText remarksInput = new EditText(this);
        remarksInput.setHint("Enter admin review remarks");
        remarksInput.setMinLines(3);
        remarksInput.setGravity(android.view.Gravity.TOP);
        int padding = (int) (16 * getResources().getDisplayMetrics().density + 0.5f);
        remarksInput.setPadding(padding, padding, padding, padding);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Review Therapy Report")
                .setMessage("Check the submitted report and add review remarks.")
                .setView(remarksInput)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Submit Review", (dialog, which) -> {
                    String remarks = remarksInput.getText().toString().trim();

                    if (remarks.length() > 2000) {
                        Toast.makeText(
                                this,
                                "Remarks cannot exceed 2000 characters.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    reviewReport(remarks);
                })
                .show();
    }

    private void reviewReport(String remarks) {
        setRequestInProgress(true);

        AdminReportReviewRequest request =
                new AdminReportReviewRequest(remarks);

        apiService.reviewAdminReport(reportId, request).enqueue(
                new Callback<AdminReportResponse>() {
                    @Override
                    public void onResponse(
                            Call<AdminReportResponse> call,
                            Response<AdminReportResponse> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        setRequestInProgress(false);

                        if (response.isSuccessful() && response.body() != null) {
                            applyReportResponse(response.body());

                            Snackbar.make(
                                    btnAdminReportChangeStatus,
                                    "Report reviewed successfully.",
                                    Snackbar.LENGTH_LONG
                            ).show();

                            setResult(RESULT_OK);
                        } else {
                            showApiError("Unable to review report", response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminReportResponse> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed() || call.isCanceled()) {
                            return;
                        }

                        setRequestInProgress(false);

                        Toast.makeText(
                                AdminTherapyReportDetailsActivity.this,
                                "Network error while reviewing report.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void showPublishReportDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Publish Therapy Report")
                .setMessage(
                        "Publish this reviewed report? "
                                + "The parent can access it once it is published."
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Publish", (dialog, which) -> publishReport())
                .show();
    }

    private void publishReport() {
        setRequestInProgress(true);

        apiService.publishAdminReport(reportId).enqueue(
                new Callback<AdminReportResponse>() {
                    @Override
                    public void onResponse(
                            Call<AdminReportResponse> call,
                            Response<AdminReportResponse> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        setRequestInProgress(false);

                        if (response.isSuccessful() && response.body() != null) {
                            applyReportResponse(response.body());

                            Snackbar.make(
                                    btnAdminReportChangeStatus,
                                    "Therapy report published successfully.",
                                    Snackbar.LENGTH_LONG
                            ).show();

                            setResult(RESULT_OK);
                        } else {
                            showApiError("Unable to publish report", response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminReportResponse> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed() || call.isCanceled()) {
                            return;
                        }

                        setRequestInProgress(false);

                        Toast.makeText(
                                AdminTherapyReportDetailsActivity.this,
                                "Network error while publishing report.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void applyReportResponse(AdminReportResponse report) {
        reportStatus = normalizeStatus(report.getStatus());

        setText(tvAdminReportDetailChild, report.getChildName());
        setText(tvAdminReportDetailTherapist, report.getTherapistName());
        setText(tvAdminReportDetailTherapy, report.getReportTitle());
        setText(tvAdminReportDetailSummary, report.getReportContent());
        setText(tvAdminReportDetailRemarks, report.getAdminRemarks());

        updateStatusUI();
    }

    private void setRequestInProgress(boolean inProgress) {
        requestInProgress = inProgress;
        btnAdminReportChangeStatus.setEnabled(!inProgress);
        btnAdminReportBack.setEnabled(!inProgress);

        if (inProgress) {
            btnAdminReportChangeStatus.setText("Please wait...");
        } else {
            updateStatusUI();
        }
    }

    private void showApiError(String action, int code) {
        Toast.makeText(
                this,
                action + ". HTTP " + code,
                Toast.LENGTH_LONG
        ).show();
    }

    private void setupAdminReportBackButton() {
        btnAdminReportBack.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
