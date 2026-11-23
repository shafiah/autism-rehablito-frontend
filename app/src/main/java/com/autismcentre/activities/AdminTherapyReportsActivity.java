
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminReportResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.autismcentre.model.AdminBranchResponse;
import com.autismcentre.repository.AdminBranchRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminTherapyReportsActivity extends BaseAdminActivity {

    private TextInputEditText etAdminSearchReport;
    private MaterialAutoCompleteTextView actAdminReportBranch;
    private MaterialAutoCompleteTextView actAdminReportTherapy;
    private MaterialAutoCompleteTextView actAdminReportStatus;
    private LinearLayout adminReportListContainer;
    private TextView tvAdminNoReports;

    private ApiService apiService;
    private AdminBranchRepository adminBranchRepository;

    private final List<String> adminReportBranchNames = new ArrayList<>();

    private String selectedAdminReportBranch = "All Branches";
    private String selectedAdminReportTherapy = "All Therapy Types";
    private String selectedAdminReportStatus = "All Status";

    private static final int REQUEST_REPORT_DETAILS = 4001;

    private final List<AdminReportResponse> allReports = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");
        showAdminPageContent(R.layout.activity_admin_therapy_reports);

        apiService = ApiClient.getApiService();
        adminBranchRepository = new AdminBranchRepository();

        initializeAdminReportViews();
        //setupAdminReportBranchDropdown();
        setupAdminReportBranchDropdown();
        loadAdminReportBranches();
        setupAdminReportTherapyDropdown();
        setupAdminReportStatusDropdown();
        setupAdminReportSearch();

        loadAdminTherapyReports();
    }

    private void initializeAdminReportViews() {
        etAdminSearchReport = findViewById(R.id.etAdminSearchReport);
        actAdminReportBranch = findViewById(R.id.actAdminReportBranch);
        actAdminReportTherapy = findViewById(R.id.actAdminReportTherapy);
        actAdminReportStatus = findViewById(R.id.actAdminReportStatus);
        adminReportListContainer = findViewById(R.id.adminReportListContainer);
        tvAdminNoReports = findViewById(R.id.tvAdminNoReports);
    }

    private void setupAdminReportBranchDropdown() {
        adminReportBranchNames.clear();
        adminReportBranchNames.add("All Branches");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                adminReportBranchNames
        );

        actAdminReportBranch.setAdapter(adapter);
        actAdminReportBranch.setText("All Branches", false);

        actAdminReportBranch.setOnItemClickListener((parent, view, position, id) -> {
            selectedAdminReportBranch =
                    adminReportBranchNames.get(position);

            renderFilteredReports();
        });
    }

    private void loadAdminReportBranches() {
        adminBranchRepository.getAdminBranches(
                new Callback<List<AdminBranchResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminBranchResponse>> call,
                            Response<List<AdminBranchResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(
                                    AdminTherapyReportsActivity.this,
                                    "Unable to load branches.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        adminReportBranchNames.clear();
                        adminReportBranchNames.add("All Branches");

                        for (AdminBranchResponse branch : response.body()) {
                            if (branch == null
                                    || branch.getBranchName() == null
                                    || branch.getBranchName().trim().isEmpty()) {
                                continue;
                            }

                            // Only active branches should appear.
                            if (Boolean.TRUE.equals(branch.getActive())) {
                                adminReportBranchNames.add(
                                        branch.getBranchName().trim()
                                );
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                AdminTherapyReportsActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                adminReportBranchNames
                        );

                        actAdminReportBranch.setAdapter(adapter);

                        // Keep the selected branch if it still exists.
                        if (!adminReportBranchNames.contains(
                                selectedAdminReportBranch)) {
                            selectedAdminReportBranch = "All Branches";
                        }

                        actAdminReportBranch.setText(
                                selectedAdminReportBranch,
                                false
                        );

                        renderFilteredReports();
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed() || call.isCanceled()) {
                            return;
                        }

                        Toast.makeText(
                                AdminTherapyReportsActivity.this,
                                "Network error while loading branches.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
    private void setupAdminReportTherapyDropdown() {
        String[] therapies = {
                "All Therapy Types",
                "Speech Therapy",
                "Occupational Therapy"
        };

        actAdminReportTherapy.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                therapies
        ));

        actAdminReportTherapy.setText("All Therapy Types", false);

        actAdminReportTherapy.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectedAdminReportTherapy = therapies[position];
                    renderFilteredReports();
                }
        );
    }

    private void setupAdminReportStatusDropdown() {
        String[] statuses = {
                "All Status", "Submitted", "Reviewed", "Published"
        };

        actAdminReportStatus.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statuses
        ));

        actAdminReportStatus.setText("All Status", false);

        actAdminReportStatus.setOnItemClickListener((parent, view, position, id) -> {
            selectedAdminReportStatus = statuses[position];
            loadAdminTherapyReports();
        });
    }

    private void setupAdminReportSearch() {
        etAdminSearchReport.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderFilteredReports();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadAdminTherapyReports() {
        adminReportListContainer.removeAllViews();
        tvAdminNoReports.setVisibility(View.VISIBLE);
        tvAdminNoReports.setText("Loading reports...");
        allReports.clear();

        String status = mapStatusToApi(selectedAdminReportStatus);

        if (status == null) {
            loadReportsForStatus("SUBMITTED", true);
        } else {
            loadReportsForStatus(status, false);
        }
    }

    private String mapStatusToApi(String displayStatus) {
        if ("Submitted".equals(displayStatus)) {
            return "SUBMITTED";
        }
        if ("Reviewed".equals(displayStatus)) {
            return "REVIEWED";
        }
        if ("Published".equals(displayStatus)) {
            return "PUBLISHED";
        }
        return null;
    }

    private void loadReportsForStatus(String status, boolean loadAllStatuses) {
        apiService.getAdminReports(status).enqueue(new Callback<List<AdminReportResponse>>() {
            @Override
            public void onResponse(
                    Call<List<AdminReportResponse>> call,
                    Response<List<AdminReportResponse>> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    tvAdminNoReports.setText(
                            "Unable to load reports. HTTP " + response.code());
                    tvAdminNoReports.setVisibility(View.VISIBLE);
                    return;
                }

                allReports.addAll(response.body());

                if (loadAllStatuses && "SUBMITTED".equals(status)) {

                    loadReportsForStatus("REVIEWED", true);

                } else if (loadAllStatuses && "REVIEWED".equals(status)) {

                    loadReportsForStatus("PUBLISHED", false);

                } else {

                    renderFilteredReports();
                }
            }

            @Override
            public void onFailure(
                    Call<List<AdminReportResponse>> call,
                    Throwable t) {

                if (isFinishing() || isDestroyed() || call.isCanceled()) {
                    return;
                }

                tvAdminNoReports.setText("Network error while loading reports.");
                tvAdminNoReports.setVisibility(View.VISIBLE);
            }
        });
    }

    private void renderFilteredReports() {
        adminReportListContainer.removeAllViews();

        // Latest submitted report first (descending date order).
        Collections.sort(allReports, (first, second) -> {

            String firstDate = first.getSubmittedAt();
            String secondDate = second.getSubmittedAt();

            if (firstDate != null && secondDate != null) {

                int dateComparison =
                        secondDate.compareTo(firstDate);

                if (dateComparison != 0) {
                    return dateComparison;
                }

            } else if (firstDate == null && secondDate != null) {
                return 1;

            } else if (firstDate != null) {
                return -1;
            }

            // Fallback: higher report ID first.
            Long firstId = first.getReportId();
            Long secondId = second.getReportId();

            if (firstId == null) return 1;
            if (secondId == null) return -1;

            return Long.compare(secondId, firstId);
        });

        String query = etAdminSearchReport.getText() == null
                ? ""
                : etAdminSearchReport.getText().toString().trim().toLowerCase(Locale.ROOT);

        int shown = 0;

        for (AdminReportResponse report : allReports) {
            if (report == null) {
                continue;
            }

            String childName = safe(report.getChildName());
            String therapistName = safe(report.getTherapistName());
            String title = safe(report.getReportTitle());
            String status = safe(report.getStatus());
            String branchName = safe(report.getBranchName());
            String therapyType = safe(report.getTherapyType());

            if (!query.isEmpty()
                    && !childName.toLowerCase(Locale.ROOT).contains(query)
                    && !therapistName.toLowerCase(Locale.ROOT).contains(query)
                    && !title.toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }

            if (!"All Branches".equals(selectedAdminReportBranch)
                    && !branchName.equalsIgnoreCase(selectedAdminReportBranch)) {
                continue;
            }

            if (!"All Therapy Types".equals(selectedAdminReportTherapy)
                    && !therapyType.equalsIgnoreCase(selectedAdminReportTherapy)) {
                continue;
            }

            if (!statusMatchesSelectedFilter(status)) {
                continue;
            }

            //if (!therapyMatchesSelectedFilter(title)) {
              //  continue;
            //}

            addAdminTherapyReport(report);
            shown++;
        }

        if (shown == 0) {
            tvAdminNoReports.setText("No therapy reports found.");
            tvAdminNoReports.setVisibility(View.VISIBLE);
        } else {
            tvAdminNoReports.setVisibility(View.GONE);
        }
    }

    private boolean statusMatchesSelectedFilter(String status) {
        if ("All Status".equals(selectedAdminReportStatus)) {
            return true;
        }

        return status.equalsIgnoreCase(selectedAdminReportStatus);
    }

    private boolean therapyMatchesSelectedFilter(String title) {
        if ("All Therapy Types".equals(selectedAdminReportTherapy)) {
            return true;
        }

        return title.toLowerCase(Locale.ROOT).contains(
                selectedAdminReportTherapy.toLowerCase(Locale.ROOT));
    }

    private void addAdminTherapyReport(AdminReportResponse report) {
        View reportView = LayoutInflater.from(this).inflate(
                R.layout.item_admin_therapy_report,
                adminReportListContainer,
                false
        );

        TextView tvChild = reportView.findViewById(R.id.tvAdminReportChild);
        TextView tvTherapist = reportView.findViewById(R.id.tvAdminReportTherapist);
        TextView tvTherapy = reportView.findViewById(R.id.tvAdminReportTherapy);
        TextView tvBranch = reportView.findViewById(R.id.tvAdminReportBranch);
        TextView tvDate = reportView.findViewById(R.id.tvAdminReportDate);
        TextView tvStatus = reportView.findViewById(R.id.tvAdminReportStatus);

        tvChild.setText(safe(report.getChildName()));
        tvTherapist.setText("Therapist: " + safe(report.getTherapistName()));

        // The current report DTO does not include therapy type or branch.
        String therapyType = safe(report.getTherapyType());

        tvTherapy.setText(therapyType);
        tvBranch.setText("Branch: " + safe(report.getBranchName()));
        tvDate.setText("Submitted: " + formatDate(report.getSubmittedAt()));
        tvStatus.setText(formatStatus(report.getStatus()));

        reportView.setOnClickListener(v -> openReportDetails(report));

        adminReportListContainer.addView(reportView);
    }

    private void openReportDetails(AdminReportResponse report) {
        Intent intent = new Intent(
                AdminTherapyReportsActivity.this,
                AdminTherapyReportDetailsActivity.class
        );

        intent.putExtra("report_id", report.getReportId());
        intent.putExtra("report_child", safe(report.getChildName()));
        intent.putExtra("report_therapist", safe(report.getTherapistName()));
        intent.putExtra("report_therapy", safe(report.getReportTitle()));
        intent.putExtra("report_date", formatDate(report.getSubmittedAt()));
        intent.putExtra("report_status", formatStatus(report.getStatus()));
        intent.putExtra("report_summary", safe(report.getReportContent()));
        intent.putExtra("report_observations", "");
        intent.putExtra("report_progress", "");
        intent.putExtra("report_remarks", safe(report.getAdminRemarks()));

        startActivityForResult(intent, REQUEST_REPORT_DETAILS);
    }

    private String formatStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Unknown";
        }

        String lower = status.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private String formatDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Not available";
        }

        try {
            String datePart = value.length() >= 10 ? value.substring(0, 10) : value;
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(datePart);

            if (date != null) {
                return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(date);
            }
        } catch (Exception ignored) {
        }

        return value;
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "Not available" : value;
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_REPORT_DETAILS && resultCode == RESULT_OK) {
            loadAdminTherapyReports();
        }
    }
}
