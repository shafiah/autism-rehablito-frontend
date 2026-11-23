
package com.autismcentre.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.model.AdminSessionResponse;
import com.autismcentre.repository.AdminSessionRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSessionDetailsActivity extends BaseAdminActivity {

    private static final int REQUEST_EDIT_SESSION = 2001;

    private TextView tvChild;
    private TextView tvTherapist;
    private TextView tvTherapy;
    private TextView tvBranch;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvStatus;

    private MaterialButton btnEditSession;
    private MaterialButton btnCancelSession;

    private AdminSessionRepository sessionRepository;
    private AdminSessionResponse currentSession;

    private Long sessionId;
    private boolean requestInProgress = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");
        showAdminPageContent(R.layout.activity_admin_session_details);

        initializeAdminSessionDetailsViews();

        sessionRepository = new AdminSessionRepository();

        // ID is passed by the session list card.
        sessionId = getIntent().getLongExtra("session_id", -1L);

        if (sessionId == null || sessionId <= 0) {
            showToast("Session ID nahi mili. List se dobara open karo.");
            btnEditSession.setEnabled(false);
            btnCancelSession.setEnabled(false);
            return;
        }

        btnEditSession.setOnClickListener(v -> openEditSession());
        btnCancelSession.setOnClickListener(v -> showCancelConfirmation());

        loadAdminSessionDetails();
    }

    private void initializeAdminSessionDetailsViews() {
        tvChild = findViewById(R.id.tvAdminSessionDetailChild);
        tvTherapist = findViewById(R.id.tvAdminSessionDetailTherapist);
        tvTherapy = findViewById(R.id.tvAdminSessionDetailTherapy);
        tvBranch = findViewById(R.id.tvAdminSessionDetailBranch);
        tvDate = findViewById(R.id.tvAdminSessionDetailDate);
        tvTime = findViewById(R.id.tvAdminSessionDetailTime);
        tvStatus = findViewById(R.id.tvAdminSessionDetailStatus);

        btnEditSession = findViewById(R.id.btnAdminEditSession);
        btnCancelSession = findViewById(R.id.btnAdminCancelSession);
    }

    private void loadAdminSessionDetails() {
        if (requestInProgress) {
            return;
        }

        requestInProgress = true;
        btnEditSession.setEnabled(false);
        btnCancelSession.setEnabled(false);

        sessionRepository.getSessionById(
                sessionId,
                new Callback<AdminSessionResponse>() {
                    @Override
                    public void onResponse(
                            Call<AdminSessionResponse> call,
                            Response<AdminSessionResponse> response) {

                        requestInProgress = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            currentSession = response.body();
                            updateAdminSessionDetailsUI();
                        } else {
                            btnEditSession.setEnabled(false);
                            btnCancelSession.setEnabled(false);
                            showToast("Session details load nahi hui. HTTP "
                                    + response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminSessionResponse> call,
                            Throwable t) {

                        requestInProgress = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        btnEditSession.setEnabled(false);
                        btnCancelSession.setEnabled(false);

                        showToast("Session details ke liye server connect nahi hua.");
                        android.util.Log.e(
                                "AdminSessionDetails",
                                "Failed to load session " + sessionId,
                                t
                        );
                    }
                }
        );
    }

    private void updateAdminSessionDetailsUI() {
        if (currentSession == null) {
            return;
        }

        tvChild.setText(safeText(currentSession.getChildName()));
        tvTherapist.setText(safeText(currentSession.getTherapistName()));
        tvTherapy.setText(safeText(currentSession.getTherapyType()));
        tvBranch.setText(safeText(currentSession.getBranchName()));

        tvDate.setText(formatDate(currentSession.getSessionDate()));

        tvTime.setText(
                formatTime(currentSession.getStartTime())
                        + " - "
                        + formatTime(currentSession.getEndTime())
        );

        String status = formatStatus(currentSession.getStatus());
        tvStatus.setText(status);

        boolean isScheduled = "SCHEDULED".equalsIgnoreCase(
                currentSession.getStatus()
        );

        boolean isCompleted = "COMPLETED".equalsIgnoreCase(
                currentSession.getStatus()
        );

        boolean isCancelled = "CANCELLED".equalsIgnoreCase(
                currentSession.getStatus()
        );

        if (isCompleted) {
            tvStatus.setTextColor(Color.parseColor("#388E3C"));
        } else if (isCancelled) {
            tvStatus.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        }

        // Backend permits editing/cancelling only SCHEDULED sessions.
        btnEditSession.setEnabled(isScheduled);
        btnCancelSession.setEnabled(isScheduled);
        btnCancelSession.setVisibility(
                isScheduled ? View.VISIBLE : View.GONE
        );
    }

    private void openEditSession() {
        if (currentSession == null) {
            showToast("Session details abhi load nahi hui.");
            return;
        }

        if (!"SCHEDULED".equalsIgnoreCase(currentSession.getStatus())) {
            showToast("Sirf Scheduled session edit ho sakta hai.");
            return;
        }

        Intent intent = new Intent(
                AdminSessionDetailsActivity.this,
                AdminEditSessionActivity.class
        );

        // The edit screen should use this ID to load/update the real record.
        intent.putExtra("session_id", sessionId);

        // Keep these extras for compatibility with the existing edit screen.
        intent.putExtra("session_child", currentSession.getChildName());
        intent.putExtra("session_therapist", currentSession.getTherapistName());
        intent.putExtra("session_therapy", currentSession.getTherapyType());
        intent.putExtra("session_branch", currentSession.getBranchName());
        intent.putExtra("session_date", currentSession.getSessionDate());
        intent.putExtra(
                "session_time",
                currentSession.getStartTime()
        );
        intent.putExtra("session_end_time", currentSession.getEndTime());
        intent.putExtra("session_status", currentSession.getStatus());
        intent.putExtra("session_remarks", currentSession.getRemarks());

        startActivityForResult(intent, REQUEST_EDIT_SESSION);
    }

    private void showCancelConfirmation() {
        if (currentSession == null || requestInProgress) {
            return;
        }

        if (!"SCHEDULED".equalsIgnoreCase(currentSession.getStatus())) {
            showToast("Sirf Scheduled session cancel ho sakta hai.");
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel Session?")
                .setMessage(
                        "Kya aap " + safeText(currentSession.getChildName())
                                + " ka session cancel karna chahte hain?"
                )
                .setNegativeButton("Keep Session", null)
                .setPositiveButton("Cancel Session", (dialog, which) ->
                        cancelSessionOnServer())
                .show();
    }

    private void cancelSessionOnServer() {
        if (sessionId == null || requestInProgress) {
            return;
        }

        requestInProgress = true;
        btnCancelSession.setEnabled(false);
        btnEditSession.setEnabled(false);

        sessionRepository.cancelSession(
                sessionId,
                new Callback<AdminSessionResponse>() {
                    @Override
                    public void onResponse(
                            Call<AdminSessionResponse> call,
                            Response<AdminSessionResponse> response) {

                        requestInProgress = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            currentSession = response.body();
                            updateAdminSessionDetailsUI();

                            setResult(RESULT_OK);
                            showToast("Session successfully cancelled.");
                        } else {
                            btnCancelSession.setEnabled(true);
                            btnEditSession.setEnabled(true);
                            showToast("Session cancel nahi hua. HTTP "
                                    + response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminSessionResponse> call,
                            Throwable t) {

                        requestInProgress = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        btnCancelSession.setEnabled(true);
                        btnEditSession.setEnabled(true);

                        showToast("Session cancel karte waqt server error aaya.");
                        android.util.Log.e(
                                "AdminSessionDetails",
                                "Failed to cancel session " + sessionId,
                                t
                        );
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_SESSION && resultCode == RESULT_OK) {
            // Reload authoritative values from the backend after editing.
            loadAdminSessionDetails();
            setResult(RESULT_OK);
        }
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty()
                ? "Not provided"
                : value;
    }

    private String formatDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return "Not provided";
        }

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

        // Handles backend values such as 09:30:00.
        if (time.length() >= 5) {
            return time.substring(0, 5);
        }

        return time;
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

    private void showToast(String message) {
        Toast.makeText(
                AdminSessionDetailsActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
