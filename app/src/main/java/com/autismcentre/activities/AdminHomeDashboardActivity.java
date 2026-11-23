package com.autismcentre.activities;

import static androidx.core.content.ContextCompat.startActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminBranchResponse;
import com.autismcentre.model.AdminChildResponse;
import com.autismcentre.model.AdminTherapistResponse;
import com.autismcentre.model.AdminSessionResponse;
import com.autismcentre.model.AdminPaymentSummaryResponse;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import com.autismcentre.model.AdminPaymentResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHomeDashboardActivity extends BaseAdminActivity {

    private MaterialAutoCompleteTextView actAdminBranch;

    private ApiService apiService;

    private final List<AdminBranchResponse> branchList =
            new ArrayList<>();

    private final List<String> branchNames =
            new ArrayList<>();

    private Long selectedBranchId = null;

    private List<AdminChildResponse> dashboardChildList =
            new ArrayList<>();
    private List<AdminTherapistResponse> dashboardTherapistList =
            new ArrayList<>();
    private List<AdminSessionResponse> dashboardSessionList =
            new ArrayList<>();

    private TextView tvAdminActivityOne;
    private TextView tvAdminActivityTwo;
    private TextView tvAdminActivityThree;

    private TextView tvAdminTotalChildren;
    private TextView tvAdminTotalTherapists;
    private TextView tvAdminTodaySessions;
    private TextView tvAdminCompletedSessions;

    private TextView tvAdminCollectedAmount;
    private TextView tvAdminPendingAmount;

    private String selectedAdminBranch = "All Branches";
    private TextView tvAdminDashboardUnreadCount;
    private TextView tvAdminDashboardNotificationMessage;

    private com.google.android.material.button.MaterialButton
            btnAdminDashboardViewNotifications;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_home_dashboard
        );

        initializeAdminDashboardViews();
        setupAdminFeedbackButton();
        setupAdminDashboardNotificationButton();
        loadAdminDashboardData(selectedAdminBranch);
        loadAdminDashboardUnreadNotificationCount();

        apiService = ApiClient.getApiService();
        loadAdminBranches();
        loadAdminDashboardChildren();
        loadAdminDashboardSessions();
        loadAdminDashboardPaymentSummary();
        loadLatestAdminPaymentActivity();
    }

    /**
     * Initialize Admin Dashboard views.
     */
    private void initializeAdminDashboardViews() {

        tvAdminDashboardUnreadCount =
                findViewById(
                        R.id.tvAdminDashboardUnreadCount
                );

        tvAdminDashboardNotificationMessage =
                findViewById(
                        R.id.tvAdminDashboardNotificationMessage
                );

        btnAdminDashboardViewNotifications =
                findViewById(
                        R.id.btnAdminDashboardViewNotifications
                );

        actAdminBranch = findViewById(R.id.actAdminBranch);

        tvAdminTotalChildren =
                findViewById(R.id.tvAdminTotalChildren);

        tvAdminTotalTherapists =
                findViewById(R.id.tvAdminTotalTherapists);

        tvAdminTodaySessions =
                findViewById(R.id.tvAdminTodaySessions);

        tvAdminCompletedSessions =
                findViewById(R.id.tvAdminCompletedSessions);

        tvAdminCollectedAmount =
                findViewById(R.id.tvAdminCollectedAmount);

        tvAdminPendingAmount =
                findViewById(R.id.tvAdminPendingAmount);

        tvAdminActivityOne =
                findViewById(R.id.tvAdminActivityOne);

        tvAdminActivityTwo =
                findViewById(R.id.tvAdminActivityTwo);

        tvAdminActivityThree =
                findViewById(R.id.tvAdminActivityThree);
    }

    private void loadAdminDashboardChildren() {

        apiService.getAdminChildren().enqueue(
                new Callback<List<AdminChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminChildResponse>> call,
                            Response<List<AdminChildResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            dashboardChildList.clear();
                            dashboardChildList.addAll(response.body());

                            updateAdminDashboardChildrenCount();
                            updateLatestChildActivity();
                            loadAdminDashboardTherapists();
                            loadAdminDashboardSessions();
                            loadAdminDashboardPaymentSummary();
                            loadLatestAdminPaymentActivity();

                            Toast.makeText(
                                    AdminHomeDashboardActivity.this,
                                    selectedAdminBranch + " selected",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminChildResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                AdminHomeDashboardActivity.this,
                                "Children loading failed: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void loadAdminDashboardTherapists() {

        final Long requestedBranchId = selectedBranchId;

        apiService.getAdminTherapists(null).enqueue(
                new Callback<List<AdminTherapistResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminTherapistResponse>> call,
                            Response<List<AdminTherapistResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        // Ignore an old response if user changed branch meanwhile.
                        if (!java.util.Objects.equals(
                                requestedBranchId, selectedBranchId)) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            dashboardTherapistList.clear();
                            dashboardTherapistList.addAll(response.body());

                            updateAdminDashboardTherapistsCount();
                            updateLatestTherapistActivity();

                        } else {

                            Toast.makeText(
                                    AdminHomeDashboardActivity.this,
                                    "Unable to load therapists. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminTherapistResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                AdminHomeDashboardActivity.this,
                                "Therapist loading failed: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void loadAdminDashboardSessions() {

        final Long requestedBranchId = selectedBranchId;

        apiService.getAdminSessions(selectedBranchId).enqueue(
                new Callback<List<AdminSessionResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminSessionResponse>> call,
                            Response<List<AdminSessionResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!java.util.Objects.equals(
                                requestedBranchId, selectedBranchId)) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            dashboardSessionList.clear();
                            dashboardSessionList.addAll(response.body());

                            updateAdminDashboardSessionsCount();

                        } else {

                            Toast.makeText(
                                    AdminHomeDashboardActivity.this,
                                    "Unable to load sessions. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
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

                        Toast.makeText(
                                AdminHomeDashboardActivity.this,
                                "Sessions loading failed: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void loadAdminDashboardPaymentSummary() {

        final Long requestedBranchId = selectedBranchId;

        Call<AdminPaymentSummaryResponse> call;

        if (selectedBranchId == null) {
            call = apiService.getAdminPaymentSummary();
        } else {
            call = apiService.getAdminPaymentSummaryByBranch(selectedBranchId);
        }

        call.enqueue(new Callback<AdminPaymentSummaryResponse>() {

            @Override
            public void onResponse(
                    Call<AdminPaymentSummaryResponse> call,
                    Response<AdminPaymentSummaryResponse> response) {

                if (isFinishing() || isDestroyed()) return;

                if (!java.util.Objects.equals(
                        requestedBranchId, selectedBranchId)) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {

                    AdminPaymentSummaryResponse summary = response.body();

                    BigDecimal totalCollection = summary.getTotalCollection();
                    BigDecimal pendingAmount = summary.getPendingTransactionAmount();

                    NumberFormat currencyFormat =
                            NumberFormat.getCurrencyInstance(
                                    new Locale("en", "IN"));

                    String collected = currencyFormat.format(
                            totalCollection != null
                                    ? totalCollection
                                    : BigDecimal.ZERO);

                    String pending = currencyFormat.format(
                            pendingAmount != null
                                    ? pendingAmount
                                    : BigDecimal.ZERO);

                    tvAdminCollectedAmount.setText(collected);
                    tvAdminPendingAmount.setText(pending + " Pending");

                } else {

                    Toast.makeText(
                            AdminHomeDashboardActivity.this,
                            "Unable to load payment summary. HTTP "
                                    + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<AdminPaymentSummaryResponse> call,
                    Throwable t) {

                if (isFinishing() || isDestroyed()) return;

                Toast.makeText(
                        AdminHomeDashboardActivity.this,
                        "Payment summary loading failed: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void loadLatestAdminPaymentActivity() {

        final Long requestedBranchId = selectedBranchId;

        Call<List<AdminPaymentResponse>> call;

        if (requestedBranchId == null) {
            call = apiService.getAdminPaidPayments();
        } else {
            call = apiService.getAdminPaidPaymentsByBranch(
                    requestedBranchId
            );
        }

        call.enqueue(new Callback<List<AdminPaymentResponse>>() {

            @Override
            public void onResponse(
                    Call<List<AdminPaymentResponse>> call,
                    Response<List<AdminPaymentResponse>> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                // Ignore old response if branch changed.
                if (!java.util.Objects.equals(
                        requestedBranchId, selectedBranchId)) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {

                    AdminPaymentResponse latestPayment = null;

                    for (AdminPaymentResponse payment : response.body()) {

                        if (payment == null
                                || payment.getPaidAmount() == null) {
                            continue;
                        }

                        if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())
                                && !"PAID".equalsIgnoreCase(payment.getStatus())) {
                            continue;
                        }

                        if (latestPayment == null) {

                            latestPayment = payment;

                        } else {

                            String currentDate = payment.getPaymentDate();

                            if (currentDate == null
                                    || currentDate.trim().isEmpty()) {
                                currentDate = payment.getCreatedAt();
                            }

                            String latestDate = latestPayment.getPaymentDate();

                            if (latestDate == null
                                    || latestDate.trim().isEmpty()) {
                                latestDate = latestPayment.getCreatedAt();
                            }

                            if (currentDate != null
                                    && !currentDate.trim().isEmpty()
                                    && (latestDate == null
                                    || currentDate.compareTo(latestDate) > 0)) {

                                latestPayment = payment;
                            }
                        }
                    }

                    if (latestPayment != null) {

                        NumberFormat currencyFormat =
                                NumberFormat.getCurrencyInstance(
                                        new Locale("en", "IN")
                                );

                        String amount = currencyFormat.format(
                                latestPayment.getPaidAmount()
                        );

                        tvAdminActivityThree.setText(
                                "Payment received — " + amount
                        );

                    } else {

                        tvAdminActivityThree.setText(
                                "No successful payment yet"
                        );
                    }

                } else {

                    tvAdminActivityThree.setText(
                            "Unable to load latest payment"
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<List<AdminPaymentResponse>> call,
                    Throwable t) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (!java.util.Objects.equals(
                        requestedBranchId, selectedBranchId)) {
                    return;
                }

                tvAdminActivityThree.setText(
                        "Unable to load latest payment"
                );
            }
        });
    }


    private void updateAdminDashboardSessionsCount() {

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        int todaySessions = 0;
        int completedSessions = 0;
        int remainingSessions = 0;

        for (AdminSessionResponse session : dashboardSessionList) {

            if (session == null
                    || session.getSessionDate() == null
                    || !today.equals(session.getSessionDate())) {
                continue;
            }

            String status = session.getStatus();

            if (status == null) {
                continue;
            }

            if ("CANCELLED".equalsIgnoreCase(status)) {
                continue;
            }

            todaySessions++;

            if ("COMPLETED".equalsIgnoreCase(status)) {

                completedSessions++;

            } else if ("SCHEDULED".equalsIgnoreCase(status)
                    || "IN_PROGRESS".equalsIgnoreCase(status)) {

                remainingSessions++;
            }
        }

        tvAdminTodaySessions.setText(
                todaySessions + " Sessions Scheduled"
        );

        tvAdminCompletedSessions.setText(
                completedSessions + " Completed  •  "
                        + remainingSessions + " Remaining"
        );
    }

    private void updateAdminDashboardTherapistsCount() {

        int totalTherapists = 0;

        for (AdminTherapistResponse therapist : dashboardTherapistList) {

            if (therapist == null) {
                continue;
            }

            boolean matchesBranch =
                    selectedBranchId == null
                            || selectedBranchId.equals(
                            therapist.getBranchId());

            if (matchesBranch) {
                totalTherapists++;
            }
        }

        tvAdminTotalTherapists.setText(
                String.valueOf(totalTherapists)
        );
    }
    private void updateAdminDashboardChildrenCount() {

        int totalChildren = 0;

        for (AdminChildResponse child : dashboardChildList) {

            if (child == null) {
                continue;
            }

            if (selectedBranchId == null
                    || (child.getBranchId() != null
                    && selectedBranchId.equals(child.getBranchId()))) {

                totalChildren++;
            }
        }

        tvAdminTotalChildren.setText(
                String.valueOf(totalChildren)
        );
    }

    private void updateLatestChildActivity() {

        AdminChildResponse latestChild = null;

        for (AdminChildResponse child : dashboardChildList) {

            if (child == null || child.getCreatedAt() == null) {
                continue;
            }

            if (selectedBranchId != null
                    && !selectedBranchId.equals(child.getBranchId())) {
                continue;
            }

            if (latestChild == null
                    || child.getCreatedAt().compareTo(
                    latestChild.getCreatedAt()) > 0) {

                latestChild = child;
            }
        }

        if (latestChild != null) {

            tvAdminActivityOne.setText(
                    "New child registered — " + latestChild.getChildName()
            );

        } else {

            tvAdminActivityOne.setText(
                    "No child registered yet"
            );
        }
    }

    private void updateLatestTherapistActivity() {

        AdminTherapistResponse latestTherapist = null;

        for (AdminTherapistResponse therapist : dashboardTherapistList) {

            if (therapist == null || therapist.getCreatedAt() == null) {
                continue;
            }

            if (selectedBranchId != null
                    && !selectedBranchId.equals(therapist.getBranchId())) {
                continue;
            }

            if (latestTherapist == null
                    || therapist.getCreatedAt().compareTo(
                    latestTherapist.getCreatedAt()) > 0) {

                latestTherapist = therapist;
            }
        }

        if (latestTherapist != null) {

            tvAdminActivityTwo.setText(
                    "New therapist added — " + latestTherapist.getName()
            );

        } else {

            tvAdminActivityTwo.setText(
                    "No therapist added yet"
            );
        }
    }

    private void setupAdminDashboardNotificationButton() {

        btnAdminDashboardViewNotifications.setOnClickListener(
                v -> {

                    android.content.Intent intent =
                            new android.content.Intent(
                                    AdminHomeDashboardActivity.this,
                                    AdminNotificationManagementActivity.class
                            );

                    startActivity(intent);
                }
        );
    }

    private void loadAdminDashboardUnreadNotificationCount() {

        /*
         * Demo notification data.
         *
         * Backend integration ke baad ye count
         * database/API se aayega.
         */

        int unreadNotificationCount = 2;

        updateAdminDashboardUnreadNotificationUI(
                unreadNotificationCount
        );
    }

    private void updateAdminDashboardUnreadNotificationUI(
            int unreadCount
    ) {

        tvAdminDashboardUnreadCount.setText(
                unreadCount + " Unread"
        );

        if (unreadCount == 0) {

            tvAdminDashboardNotificationMessage.setText(
                    "You have no unread notifications."
            );

            tvAdminDashboardUnreadCount.setVisibility(
                    android.view.View.GONE
            );

        } else {

            tvAdminDashboardNotificationMessage.setText(
                    "You have unread notifications."
            );

            tvAdminDashboardUnreadCount.setVisibility(
                    android.view.View.VISIBLE
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (
                tvAdminDashboardUnreadCount != null
                        && tvAdminDashboardNotificationMessage != null
        ) {

            loadAdminDashboardUnreadNotificationCount();
        }
    }

    /**
     * Setup Admin Branch Dropdown.
     */
    private void loadAdminBranches() {

        apiService.getAdminBranches().enqueue(
                new Callback<List<AdminBranchResponse>>() {

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
                            branchNames.clear();

                            // Default option
                            branchNames.add("All Branches");
                            selectedBranchId = null;
                            selectedAdminBranch = "All Branches";

                            for (AdminBranchResponse branch : response.body()) {

                                if (branch != null
                                        && branch.getId() != null
                                        && branch.getBranchName() != null
                                        && Boolean.TRUE.equals(branch.getActive())) {

                                    branchList.add(branch);
                                    branchNames.add(branch.getBranchName());
                                }
                            }

                            setupAdminBranchDropdown();

                        } else {

                            Toast.makeText(
                                    AdminHomeDashboardActivity.this,
                                    "Unable to load branches",
                                    Toast.LENGTH_SHORT
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
                                AdminHomeDashboardActivity.this,
                                "Branch loading failed: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void setupAdminBranchDropdown() {

        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                branchNames
        );

        actAdminBranch.setAdapter(branchAdapter);

        actAdminBranch.setText("All Branches", false);

        actAdminBranch.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position == 0) {

                        selectedBranchId = null;
                        selectedAdminBranch = "All Branches";

                    } else {

                        AdminBranchResponse selectedBranch =
                                branchList.get(position - 1);

                        selectedBranchId = selectedBranch.getId();

                        selectedAdminBranch =
                                selectedBranch.getBranchName();
                    }
                    updateAdminDashboardChildrenCount();
                    updateLatestChildActivity();
                    // Reload therapists for selected branch
                    loadAdminDashboardTherapists();
                    // Reload sessions for selected branch
                    loadAdminDashboardSessions();
                    // Reload payment summary for selected branch
                    loadAdminDashboardPaymentSummary();
                    loadLatestAdminPaymentActivity();

                    Toast.makeText(
                            AdminHomeDashboardActivity.this,
                            selectedAdminBranch + " selected",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );
    }

    // =====================================================
// ADMIN FEEDBACK NAVIGATION
// =====================================================

    private void setupAdminFeedbackButton() {

        findViewById(R.id.btnAdminFeedback)
                .setOnClickListener(v -> {

                    android.content.Intent intent =
                            new android.content.Intent(
                                    AdminHomeDashboardActivity.this,
                                    AdminFeedbackActivity.class
                            );

                    startActivity(intent);
                });
    }


    /**
     * Load dashboard data according to selected branch.
     *
     * Currently demo/static data.
     * Later this method will receive data from backend API.
     */
    private void loadAdminDashboardData(String branch) {

        int totalChildren;
        int totalTherapists;
        int todaySessions;
        int completedSessions;

        int totalCollectedAmount;
        int totalPendingAmount;

        switch (branch) {

            case "Patna":
                totalChildren = 18;
                totalTherapists = 5;
                todaySessions = 7;
                completedSessions = 3;

                totalCollectedAmount = 4000;
                totalPendingAmount = 11000;
                break;

            case "Siwan":
                totalChildren = 8;
                totalTherapists = 2;
                todaySessions = 3;
                completedSessions = 1;

                totalCollectedAmount = 6000;
                totalPendingAmount = 0;
                break;

            case "Madhubani":
                totalChildren = 6;
                totalTherapists = 1;
                todaySessions = 2;
                completedSessions = 1;

                totalCollectedAmount = 0;
                totalPendingAmount = 0;
                break;

            case "Muzaffarpur":
                totalChildren = 7;
                totalTherapists = 2;
                todaySessions = 3;
                completedSessions = 2;

                totalCollectedAmount = 6000;
                totalPendingAmount = 0;
                break;

            case "Hajipur":
                totalChildren = 5;
                totalTherapists = 1;
                todaySessions = 2;
                completedSessions = 1;

                totalCollectedAmount = 0;
                totalPendingAmount = 5500;
                break;

            case "Ramnagri":
                totalChildren = 4;
                totalTherapists = 1;
                todaySessions = 1;
                completedSessions = 0;

                totalCollectedAmount = 3000;
                totalPendingAmount = 3000;
                break;

            case "All Branches":
            default:
                totalChildren = 48;
                totalTherapists = 12;
                todaySessions = 18;
                completedSessions = 8;

                totalCollectedAmount = 19000;
                totalPendingAmount = 19500;
                break;
        }

        int remainingSessions =
                todaySessions - completedSessions;

        updateAdminDashboardUI(
                totalChildren,
                totalTherapists,
                todaySessions,
                completedSessions,
                remainingSessions,
                totalCollectedAmount,
                totalPendingAmount
        );
    }

    private String formatAdminAmount(int amount) {

        return String.format(
                java.util.Locale.US,
                "%,d",
                amount
        );
    }

    /**
     * Update Admin Dashboard UI.
     */
    private void updateAdminDashboardUI(
            int totalChildren,
            int totalTherapists,
            int todaySessions,
            int completedSessions,
            int remainingSessions,
            int totalCollectedAmount,
            int totalPendingAmount) {

        tvAdminTotalChildren.setText(
                String.valueOf(totalChildren)
        );

        tvAdminTotalTherapists.setText(
                String.valueOf(totalTherapists)
        );

        tvAdminTodaySessions.setText(
                todaySessions + " Sessions Scheduled"
        );

        tvAdminCompletedSessions.setText(
                completedSessions
                        + " Completed  •  "
                        + remainingSessions
                        + " Remaining"
        );

        tvAdminCollectedAmount.setText(
                "₹" + formatAdminAmount(
                        totalCollectedAmount
                )
        );

        tvAdminPendingAmount.setText(
                "₹" + formatAdminAmount(
                        totalPendingAmount
                ) + " Pending"
        );
    }
}