
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.model.ParentReportResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends BaseParentActivity {

    private static final String TAG = "ReportsActivity";

    private TextView tvReportsChildName;
    private TextView tvLatestReportTitle;
    private TextView tvLatestReportTherapist;
    private TextView tvLatestReportDate;

    private TextView tvSpeechReportTitle;
    private TextView tvSpeechReportTherapist;
    private TextView tvSpeechReportDate;

    private TextView tvPreviousReportTitle;
    private TextView tvPreviousReportTherapist;
    private TextView tvPreviousReportDate;

    private MaterialCardView cardLatestReport;
    private MaterialCardView cardSpeechReport;
    private MaterialCardView cardPreviousOccupationalReport;

    private MaterialButton btnViewLatestReport;
    private MaterialButton btnViewSpeechReport;
    private MaterialButton btnViewPreviousReport;

    private Long currentChildId;
    private final List<ParentReportResponse> reports = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_reports);
        setPageTitle("Autism Centre");

        ApiClient.initialize(getApplicationContext());

        initializeViews();
        loadChild();
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.nav_reports;
    }

    private void initializeViews() {
        tvReportsChildName = findViewById(R.id.tvReportsChildName);

        tvLatestReportTitle = findViewById(R.id.tvLatestReportTitle);
        tvLatestReportTherapist =
                findViewById(R.id.tvLatestReportTherapist);
        tvLatestReportDate = findViewById(R.id.tvLatestReportDate);

        tvSpeechReportTitle = findViewById(R.id.tvSpeechReportTitle);
        tvSpeechReportTherapist =
                findViewById(R.id.tvSpeechReportTherapist);
        tvSpeechReportDate = findViewById(R.id.tvSpeechReportDate);

        tvPreviousReportTitle =
                findViewById(R.id.tvPreviousReportTitle);
        tvPreviousReportTherapist =
                findViewById(R.id.tvPreviousReportTherapist);
        tvPreviousReportDate =
                findViewById(R.id.tvPreviousReportDate);

        cardLatestReport = findViewById(R.id.cardLatestReport);
        cardSpeechReport = findViewById(R.id.cardSpeechReport);
        cardPreviousOccupationalReport =
                findViewById(R.id.cardPreviousOccupationalReport);

        btnViewLatestReport =
                findViewById(R.id.btnViewLatestReport);
        btnViewSpeechReport =
                findViewById(R.id.btnViewSpeechReport);
        btnViewPreviousReport =
                findViewById(R.id.btnViewPreviousReport);

        tvReportsChildName.setText("Loading...");

        setReportCardLoading(cardLatestReport);
        setReportCardLoading(cardSpeechReport);
        setReportCardLoading(cardPreviousOccupationalReport);

        btnViewLatestReport.setOnClickListener(v -> openReport(0));
        btnViewSpeechReport.setOnClickListener(v -> openReport(1));
        btnViewPreviousReport.setOnClickListener(v -> openReport(2));
    }

    private void setReportCardLoading(MaterialCardView card) {
        card.setVisibility(View.GONE);
    }

    private void loadChild() {
        ApiClient.getApiService().getMyChildren()
                .enqueue(new Callback<List<ParentChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentChildResponse>> call,
                            Response<List<ParentChildResponse>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showError("Unable to load child profile.");
                            showEmptyState();
                            return;
                        }

                        List<ParentChildResponse> children = response.body();

                        if (children.isEmpty()) {
                            tvReportsChildName.setText("No child linked");
                            showEmptyState();
                            return;
                        }

                        ParentChildResponse child = children.get(0);
                        currentChildId = child.getChildId();

                        String childName = child.getChildName();
                        tvReportsChildName.setText(
                                isBlank(childName)
                                        ? "Child Reports"
                                        : childName
                        );

                        if (currentChildId == null) {
                            showEmptyState();
                            showError("Child ID is unavailable.");
                            return;
                        }

                        loadReports(currentChildId);
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentChildResponse>> call,
                            Throwable t) {

                        Log.e(TAG, "Failed to load child", t);
                        tvReportsChildName.setText("Child Reports");
                        showEmptyState();
                        showError("Network error while loading child.");
                    }
                });
    }

    private void loadReports(Long childId) {
        ApiClient.getApiService().getMyChildReports(childId)
                .enqueue(new Callback<List<ParentReportResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentReportResponse>> call,
                            Response<List<ParentReportResponse>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            Log.e(TAG,
                                    "Reports API failed: " + response.code());
                            showEmptyState();
                            showError("Unable to load reports.");
                            return;
                        }

                        reports.clear();
                        reports.addAll(response.body());

                        // Sort newest available report date first.
                        Collections.sort(reports,
                                new Comparator<ParentReportResponse>() {
                                    @Override
                                    public int compare(
                                            ParentReportResponse first,
                                            ParentReportResponse second) {

                                        return getReportDate(second).compareTo(
                                                getReportDate(first));
                                    }
                                });

                        displayReports();
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentReportResponse>> call,
                            Throwable t) {

                        Log.e(TAG, "Failed to load reports", t);
                        showEmptyState();
                        showError("Network error while loading reports.");
                    }
                });
    }

    private void displayReports() {
        hideAllReportCards();

        if (reports.isEmpty()) {
            showError("No published reports are available yet.");
            return;
        }

        // First report is the latest; the next two are previous reports.
        bindReportCard(
                0,
                cardLatestReport,
                tvLatestReportTitle,
                tvLatestReportTherapist,
                tvLatestReportDate
        );

        bindReportCard(
                1,
                cardSpeechReport,
                tvSpeechReportTitle,
                tvSpeechReportTherapist,
                tvSpeechReportDate
        );

        bindReportCard(
                2,
                cardPreviousOccupationalReport,
                tvPreviousReportTitle,
                tvPreviousReportTherapist,
                tvPreviousReportDate
        );
    }

    private void bindReportCard(
            int index,
            MaterialCardView card,
            TextView titleView,
            TextView therapistView,
            TextView dateView) {

        if (index >= reports.size()) {
            card.setVisibility(View.GONE);
            return;
        }

        ParentReportResponse report = reports.get(index);

        titleView.setText(
                isBlank(report.getReportTitle())
                        ? "Therapy Report"
                        : report.getReportTitle()
        );

        therapistView.setText(
                "Therapist: " +
                        (isBlank(report.getTherapistName())
                                ? "Not specified"
                                : report.getTherapistName())
        );

        String date = formatDate(getReportDate(report));

        dateView.setText(
                isBlank(date) ? "Report date unavailable"
                        : "Report Date: " + date
        );

        card.setVisibility(View.VISIBLE);
    }

    private void openReport(int index) {
        if (index < 0 || index >= reports.size()) {
            showError("This report is not available.");
            return;
        }

        ParentReportResponse report = reports.get(index);

        Intent intent = new Intent(
                ReportsActivity.this,
                ReportDetailActivity.class
        );

        intent.putExtra("childId", report.getChildId());
        intent.putExtra("reportId", report.getReportId());

        startActivity(intent);
    }

    private void hideAllReportCards() {
        cardLatestReport.setVisibility(View.GONE);
        cardSpeechReport.setVisibility(View.GONE);
        cardPreviousOccupationalReport.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        hideAllReportCards();
    }

    private String getReportDate(ParentReportResponse report) {
        if (!isBlank(report.getPublishedAt())) {
            return report.getPublishedAt();
        }

        if (!isBlank(report.getSubmittedAt())) {
            return report.getSubmittedAt();
        }

        return report.getCreatedAt() == null
                ? ""
                : report.getCreatedAt();
    }

    private String formatDate(String value) {
        if (isBlank(value)) {
            return "";
        }

        String date = value.trim();
        int separator = date.indexOf('T');

        return separator > 0
                ? date.substring(0, separator)
                : date;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
