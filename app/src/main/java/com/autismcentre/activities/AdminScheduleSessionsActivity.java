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
import com.autismcentre.model.AdminBranchResponse;
import com.autismcentre.model.AdminSessionResponse;
import com.autismcentre.repository.AdminSessionRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminScheduleSessionsActivity extends BaseAdminActivity {

    private TextInputEditText etAdminSearchSession;

    private MaterialAutoCompleteTextView actAdminSessionBranch;
    private MaterialAutoCompleteTextView actAdminSessionStatus;

    private LinearLayout adminSessionListContainer;
    private TextView tvAdminNoSessions;
    private MaterialButton btnAdminAddSession;

    private AdminSessionRepository sessionRepository;

    private final List<AdminSessionResponse> sessionList =
            new ArrayList<>();

    private final List<AdminBranchResponse> branchList =
            new ArrayList<>();

    private final List<String> branchNames =
            new ArrayList<>();

    private final List<String> statusNames =
            new ArrayList<>();

    private Long selectedBranchId = null;
    private String selectedBranchName = "All Branches";
    private String selectedStatus = "All Status";
    private String currentSearchQuery = "";

    private static final int REQUEST_SESSION_DETAILS = 3001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_schedule_sessions
        );

        initializeAdminScheduleViews();

        sessionRepository = new AdminSessionRepository();

        setupAdminSessionStatusDropdown();
        setupAdminSessionSearch();
        setupAdminAddSessionButton();

        loadAdminBranches();
        loadAdminSessions();
    }

    private void initializeAdminScheduleViews() {
        etAdminSearchSession =
                findViewById(R.id.etAdminSearchSession);

        actAdminSessionBranch =
                findViewById(R.id.actAdminSessionBranch);

        actAdminSessionStatus =
                findViewById(R.id.actAdminSessionStatus);

        adminSessionListContainer =
                findViewById(R.id.adminSessionListContainer);

        tvAdminNoSessions =
                findViewById(R.id.tvAdminNoSessions);

        btnAdminAddSession =
                findViewById(R.id.btnAdminAddSession);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh database sessions when returning from another screen.
        if (sessionRepository != null) {
            loadAdminSessions();
        }
    }

    private void loadAdminBranches() {
        ApiClient.getApiService().getAdminBranches()
                .enqueue(new Callback<List<AdminBranchResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<AdminBranchResponse>> call,
                            Response<List<AdminBranchResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            branchList.clear();
                            branchList.addAll(response.body());

                            setupAdminSessionBranchDropdown();

                        } else {
                            Toast.makeText(
                                    AdminScheduleSessionsActivity.this,
                                    "Branches load nahi hui. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                AdminScheduleSessionsActivity.this,
                                "Branch list ke liye server se connect nahi hua.",
                                Toast.LENGTH_LONG
                        ).show();

                        android.util.Log.e(
                                "AdminSessionAPI",
                                "Failed to load branches",
                                t
                        );
                    }
                });
    }

    private void setupAdminSessionBranchDropdown() {
        branchNames.clear();
        branchNames.add("All Branches");

        for (AdminBranchResponse branch : branchList) {
            if (branch.getBranchName() != null) {
                branchNames.add(branch.getBranchName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                branchNames
        );

        actAdminSessionBranch.setAdapter(adapter);

        if (!branchNames.contains(selectedBranchName)) {
            selectedBranchName = "All Branches";
            selectedBranchId = null;
        }

        actAdminSessionBranch.setText(
                selectedBranchName,
                false
        );

        actAdminSessionBranch.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedBranchName = branchNames.get(position);
                    selectedBranchId = null;

                    if (position > 0) {
                        for (AdminBranchResponse branch : branchList) {
                            if (selectedBranchName.equals(
                                    branch.getBranchName())) {
                                selectedBranchId = branch.getId();
                                break;
                            }
                        }

                        if (selectedBranchId == null) {
                            Toast.makeText(
                                    this,
                                    "Selected branch ID nahi mili.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }
                    }

                    loadAdminSessions();
                }
        );
    }

    private void setupAdminSessionStatusDropdown() {
        statusNames.clear();
        statusNames.add("All Status");
        statusNames.add("Scheduled");
        statusNames.add("In Progress");
        statusNames.add("Completed");
        statusNames.add("Cancelled");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statusNames
        );

        actAdminSessionStatus.setAdapter(adapter);

        actAdminSessionStatus.setText(
                selectedStatus,
                false
        );

        actAdminSessionStatus.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectedStatus = statusNames.get(position);
                    displayAdminSessions();
                }
        );
    }

    private void setupAdminAddSessionButton() {
        btnAdminAddSession.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminScheduleSessionsActivity.this,
                    AdminAddSessionActivity.class
            );

            startActivityForResult(
                    intent,
                    REQUEST_SESSION_DETAILS
            );
        });
    }

    private void setupAdminSessionSearch() {
        etAdminSearchSession.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {
                        currentSearchQuery = s.toString().trim();
                        displayAdminSessions();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }

    private void loadAdminSessions() {
        adminSessionListContainer.removeAllViews();
        tvAdminNoSessions.setVisibility(View.VISIBLE);
        tvAdminNoSessions.setText("Loading sessions...");

        sessionRepository.getSessionsByBranch(
                selectedBranchId,
                new Callback<List<AdminSessionResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<AdminSessionResponse>> call,
                            Response<List<AdminSessionResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            sessionList.clear();
                            sessionList.addAll(response.body());

// Latest created session first (descending order).
                            Collections.sort(sessionList, (first, second) -> {

                                String firstCreatedAt = first.getCreatedAt();
                                String secondCreatedAt = second.getCreatedAt();

                                if (firstCreatedAt != null && secondCreatedAt != null) {

                                    int dateComparison =
                                            secondCreatedAt.compareTo(firstCreatedAt);

                                    if (dateComparison != 0) {
                                        return dateComparison;
                                    }

                                } else if (firstCreatedAt == null && secondCreatedAt != null) {
                                    return 1;

                                } else if (firstCreatedAt != null) {
                                    return -1;
                                }

                                // Fallback when createdAt is missing or identical.
                                Long firstId = first.getSessionId();
                                Long secondId = second.getSessionId();

                                if (firstId == null) return 1;
                                if (secondId == null) return -1;

                                return Long.compare(secondId, firstId);
                            });

                            displayAdminSessions();

                        } else {
                            sessionList.clear();
                            adminSessionListContainer.removeAllViews();

                            tvAdminNoSessions.setVisibility(View.VISIBLE);
                            tvAdminNoSessions.setText(
                                    "Sessions load nahi hue. HTTP "
                                            + response.code()
                            );

                            Toast.makeText(
                                    AdminScheduleSessionsActivity.this,
                                    "Session API failed. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminSessionResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        sessionList.clear();
                        adminSessionListContainer.removeAllViews();

                        tvAdminNoSessions.setVisibility(View.VISIBLE);
                        tvAdminNoSessions.setText(
                                "Unable to connect to server"
                        );

                        android.util.Log.e(
                                "AdminSessionAPI",
                                "Failed to load sessions",
                                t
                        );

                        Toast.makeText(
                                AdminScheduleSessionsActivity.this,
                                "Server se sessions nahi mile.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void displayAdminSessions() {
        adminSessionListContainer.removeAllViews();

        String query = currentSearchQuery.toLowerCase(Locale.ROOT);
        int displayedCount = 0;

        for (AdminSessionResponse session : sessionList) {

            String childName = safeText(session.getChildName());
            String therapistName = safeText(session.getTherapistName());
            String branchName = safeText(session.getBranchName());
            String status = formatStatus(session.getStatus());

            if (!query.isEmpty()
                    && !childName.toLowerCase(Locale.ROOT).contains(query)
                    && !therapistName.toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }

            if (!"All Status".equals(selectedStatus)
                    && !selectedStatus.equals(status)) {
                continue;
            }

            addAdminSessionCard(session);
            displayedCount++;
        }

        if (displayedCount == 0) {
            tvAdminNoSessions.setVisibility(View.VISIBLE);

            if (sessionList.isEmpty()) {
                tvAdminNoSessions.setText("No sessions registered");
            } else {
                tvAdminNoSessions.setText("No sessions found");
            }
        } else {
            tvAdminNoSessions.setVisibility(View.GONE);
        }
    }

    private void addAdminSessionCard(AdminSessionResponse session) {
        View sessionView = LayoutInflater.from(this).inflate(
                R.layout.item_admin_session,
                adminSessionListContainer,
                false
        );

        TextView tvChild = sessionView.findViewById(
                R.id.tvAdminSessionChild
        );

        TextView tvTherapist = sessionView.findViewById(
                R.id.tvAdminSessionTherapist
        );

        TextView tvTherapy = sessionView.findViewById(
                R.id.tvAdminSessionTherapy
        );

        TextView tvBranch = sessionView.findViewById(
                R.id.tvAdminSessionBranch
        );

        TextView tvDateTime = sessionView.findViewById(
                R.id.tvAdminSessionDateTime
        );

        TextView tvStatus = sessionView.findViewById(
                R.id.tvAdminSessionStatus
        );

        tvChild.setText(safeText(session.getChildName()));

        tvTherapist.setText(
                "Therapist: " + safeText(session.getTherapistName())
        );

        tvTherapy.setText(safeText(session.getTherapyType()));

        tvBranch.setText(
                "Branch: " + safeText(session.getBranchName())
        );

        tvDateTime.setText(
                formatDate(session.getSessionDate())
                        + "  •  "
                        + formatTime(session.getStartTime())
                        + " - "
                        + formatTime(session.getEndTime())
        );

        tvStatus.setText(formatStatus(session.getStatus()));

        sessionView.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminScheduleSessionsActivity.this,
                    AdminSessionDetailsActivity.class
            );

            if (session.getSessionId() != null) {
                intent.putExtra(
                        "session_id",
                        session.getSessionId()
                );
            }

            startActivity(intent);
        });

        adminSessionListContainer.addView(sessionView);
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty()
                ? "Not provided"
                : value;
    }

    private String formatStatus(String status) {
        if (status == null) {
            return "Unknown";
        }

        switch (status.toUpperCase(Locale.ROOT)) {
            case "SCHEDULED":
                return "Scheduled";
            case "IN_PROGRESS":
                return "In Progress";
            case "COMPLETED":
                return "Completed";
            case "CANCELLED":
                return "Cancelled";
            default:
                return status;
        }
    }

    private String formatDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return "Date not provided";
        }

        // Display ISO dates as dd-MM-yyyy without changing stored values.
        String[] parts = date.split("-");
        if (parts.length == 3) {
            return parts[2] + "-" + parts[1] + "-" + parts[0];
        }

        return date;
    }

    private String formatTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return "--:--";
        }

        // Backend LocalTime usually returns HH:mm:ss.
        if (time.length() >= 5) {
            return time.substring(0, 5);
        }

        return time;
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SESSION_DETAILS
                && resultCode == RESULT_OK) {
            loadAdminSessions();
        }
    }
}