
package com.autismcentre.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentReportResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDetailActivity extends BaseParentActivity {

    private static final String TAG = "ReportDetailActivity";

    private TextView tvReportChildName;
    private TextView tvReportDate;
    private TextView tvReportTitle;
    private TextView tvReportTherapist;
    private TextView tvReportStatus;
    private TextView tvReportContent;
    private TextView tvReportGeneratedBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_report_detail);
        setPageTitle("Autism Centre");

        ApiClient.initialize(getApplicationContext());

        initializeViews();
        loadReportDetails();
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.nav_reports;
    }

    private void initializeViews() {
        tvReportChildName = findViewById(R.id.tvReportChildName);
        tvReportDate = findViewById(R.id.tvReportDate);
        tvReportTitle = findViewById(R.id.tvReportTitle);
        tvReportTherapist = findViewById(R.id.tvReportTherapist);
        tvReportStatus = findViewById(R.id.tvReportStatus);
        tvReportContent = findViewById(R.id.tvReportContent);
        tvReportGeneratedBy = findViewById(R.id.tvReportGeneratedBy);

        tvReportChildName.setText("Loading child information...");
        tvReportDate.setText("Report Date: Loading...");
        tvReportTitle.setText("Loading report title...");
        tvReportTherapist.setText("Therapist: Loading...");
        tvReportStatus.setText("Status: Loading...");
        tvReportContent.setText("Loading report content...");
        tvReportGeneratedBy.setVisibility(View.GONE);
    }

    private void loadReportDetails() {
        Long childId = getIntent().getLongExtra("childId", -1L);
        Long reportId = getIntent().getLongExtra("reportId", -1L);

        if (childId == null || reportId == null
                || childId <= 0 || reportId <= 0) {
            showError("Report information is missing. Please open the report again.");
            showUnavailableState();
            return;
        }

        ApiClient.getApiService()
                .getMyChildReportById(childId, reportId)
                .enqueue(new Callback<ParentReportResponse>() {

                    @Override
                    public void onResponse(
                            Call<ParentReportResponse> call,
                            Response<ParentReportResponse> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Log.e(TAG,
                                    "Report API failed: " + response.code());

                            showUnavailableState();
                            showError(
                                    "Unable to load this report. Please try again.");
                            return;
                        }

                        ParentReportResponse report = response.body();

                        // Verify that the returned report matches the
                        // report selected by the user.
                        if (report.getReportId() == null
                                || !reportId.equals(report.getReportId())
                                || report.getChildId() == null
                                || !childId.equals(report.getChildId())) {

                            Log.e(TAG, "Report response ID mismatch");
                            showUnavailableState();
                            showError("The requested report could not be verified.");
                            return;
                        }

                        displayReport(report);
                    }

                    @Override
                    public void onFailure(
                            Call<ParentReportResponse> call,
                            Throwable t) {

                        Log.e(TAG, "Failed to load report details", t);

                        showUnavailableState();
                        showError(
                                "Network error while loading report details.");
                    }
                });
    }

    private void displayReport(ParentReportResponse report) {

        String childName = report.getChildName();
        tvReportChildName.setText(
                "Child Name: " + displayValue(childName, "Not available")
        );

        String reportTitle = report.getReportTitle();
        tvReportTitle.setText(
                displayValue(reportTitle, "Therapy Report")
        );

        String therapistName = report.getTherapistName();
        tvReportTherapist.setText(
                "Therapist: "
                        + displayValue(therapistName, "Not available")
        );

        String reportDate = getReportDate(report);
        tvReportDate.setText(
                "Report Date: "
                        + displayValue(formatDate(reportDate), "Not available")
        );

        String status = report.getStatus();
        tvReportStatus.setText(
                "Status: " + displayValue(status, "Not available")
        );

        String content = report.getReportContent();
        tvReportContent.setText(
                displayValue(content, "No report content is available.")
        );

        tvReportGeneratedBy.setText(
                "Report information provided by "
                        + displayValue(therapistName, "the therapy team")
        );
        tvReportGeneratedBy.setVisibility(View.VISIBLE);
    }

    private String getReportDate(ParentReportResponse report) {
        if (!isBlank(report.getPublishedAt())) {
            return report.getPublishedAt();
        }

        if (!isBlank(report.getSubmittedAt())) {
            return report.getSubmittedAt();
        }

        return report.getCreatedAt();
    }

    private String formatDate(String value) {
        if (isBlank(value)) {
            return "";
        }

        String date = value.trim();
        int separatorIndex = date.indexOf('T');

        if (separatorIndex > 0) {
            return date.substring(0, separatorIndex);
        }

        return date;
    }

    private String displayValue(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void showUnavailableState() {
        tvReportChildName.setText("Child information unavailable");
        tvReportDate.setText("Report date unavailable");
        tvReportTitle.setText("Report unavailable");
        tvReportTherapist.setText("Therapist information unavailable");
        tvReportStatus.setText("Status unavailable");
        tvReportContent.setText(
                "The report could not be loaded. Please return to Reports and try again."
        );
        tvReportGeneratedBy.setVisibility(View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
