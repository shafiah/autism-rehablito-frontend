
package com.autismcentre.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.TherapistSessionResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistSessionDetailsActivity extends BaseTherapistActivity {

    private TextView tvSessionTherapy;
    private TextView tvSessionChild;
    private TextView tvSessionDate;
    private TextView tvSessionTime;
    private TextView tvSessionRoom;
    private TextView tvSessionStatus;
    private TextView tvSessionNote;
    private MaterialButton btnStartSession;

    private Long sessionId;
    private String currentStatus = "";
    private boolean requestInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageTitle("Rehablito Autism center");
        showPageContent(R.layout.activity_therapist_session_details);

        tvSessionTherapy = findViewById(R.id.tvSessionTherapy);
        tvSessionChild = findViewById(R.id.tvSessionChild);
        tvSessionDate = findViewById(R.id.tvSessionDate);
        tvSessionTime = findViewById(R.id.tvSessionTime);
        tvSessionRoom = findViewById(R.id.tvSessionRoom);
        tvSessionStatus = findViewById(R.id.tvSessionStatus);
        tvSessionNote = findViewById(R.id.tvSessionNote);
        btnStartSession = findViewById(R.id.btnStartSession);

        sessionId = getIntent().getLongExtra("session_id", -1L);

        if (sessionId == -1L) {
            showError("Session ID is missing.");
            btnStartSession.setEnabled(false);
            return;
        }

        btnStartSession.setEnabled(false);
        btnStartSession.setOnClickListener(v -> handleSessionAction());

        loadSessionDetails();
    }

    private void loadSessionDetails() {
        btnStartSession.setEnabled(false);

        ApiClient.getApiService()
                .getMyTherapistSessionById(sessionId)
                .enqueue(new Callback<TherapistSessionResponse>() {

                    @Override
                    public void onResponse(
                            Call<TherapistSessionResponse> call,
                            Response<TherapistSessionResponse> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            displaySession(response.body());
                        } else {
                            showError("Unable to load session. Error: "
                                    + response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TherapistSessionResponse> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        showError("Could not connect to the server.");
                        Toast.makeText(
                                TherapistSessionDetailsActivity.this,
                                t.getMessage() != null
                                        ? t.getMessage()
                                        : "Network error",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void displaySession(TherapistSessionResponse session) {
        tvSessionTherapy.setText(safe(session.getTherapyType()));
        tvSessionChild.setText(safe(session.getChildName()));
        tvSessionDate.setText(safe(session.getSessionDate()));

        tvSessionTime.setText(
                safe(session.getStartTime()) + " - "
                        + safe(session.getEndTime())
        );

        // Backend response provides branch details, not room number.
        tvSessionRoom.setText("Branch: " + safe(session.getBranchName()));

        tvSessionNote.setText(
                session.getRemarks() == null
                        || session.getRemarks().trim().isEmpty()
                        ? "No session note available."
                        : session.getRemarks()
        );

        currentStatus = session.getStatus() == null
                ? ""
                : session.getStatus().toUpperCase();

        tvSessionStatus.setText(formatStatus(currentStatus));
        tvSessionStatus.setTextColor(getStatusColor(currentStatus));

        updateActionButton();
    }

    private void updateActionButton() {
        requestInProgress = false;

        switch (currentStatus) {
            case "SCHEDULED":
                btnStartSession.setText("Start Session");
                btnStartSession.setEnabled(true);
                break;

            case "IN_PROGRESS":
                btnStartSession.setText("Complete Session");
                btnStartSession.setEnabled(true);
                break;

            case "COMPLETED":
                btnStartSession.setText("Session Completed");
                btnStartSession.setEnabled(false);
                break;

            case "CANCELLED":
                btnStartSession.setText("Session Cancelled");
                btnStartSession.setEnabled(false);
                break;

            default:
                btnStartSession.setText("Action Unavailable");
                btnStartSession.setEnabled(false);
                break;
        }
    }

    private void handleSessionAction() {
        if (requestInProgress) {
            return;
        }

        if ("SCHEDULED".equals(currentStatus)) {
            startSession();
        } else if ("IN_PROGRESS".equals(currentStatus)) {
            completeSession();
        }
    }

    private void startSession() {
        requestInProgress = true;
        btnStartSession.setEnabled(false);
        btnStartSession.setText("Starting...");

        ApiClient.getApiService()
                .startTherapistSession(sessionId)
                .enqueue(new Callback<TherapistSessionResponse>() {

                    @Override
                    public void onResponse(
                            Call<TherapistSessionResponse> call,
                            Response<TherapistSessionResponse> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            displaySession(response.body());

                            Toast.makeText(
                                    TherapistSessionDetailsActivity.this,
                                    "Session started successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            requestInProgress = false;
                            loadSessionDetails();

                            Toast.makeText(
                                    TherapistSessionDetailsActivity.this,
                                    "Unable to start session. Error: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TherapistSessionResponse> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        requestInProgress = false;
                        loadSessionDetails();

                        Toast.makeText(
                                TherapistSessionDetailsActivity.this,
                                "Could not start session. Check connection.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void completeSession() {
        requestInProgress = true;
        btnStartSession.setEnabled(false);
        btnStartSession.setText("Completing...");

        ApiClient.getApiService()
                .completeTherapistSession(sessionId)
                .enqueue(new Callback<TherapistSessionResponse>() {

                    @Override
                    public void onResponse(
                            Call<TherapistSessionResponse> call,
                            Response<TherapistSessionResponse> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            displaySession(response.body());

                            Toast.makeText(
                                    TherapistSessionDetailsActivity.this,
                                    "Session completed successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            requestInProgress = false;
                            loadSessionDetails();

                            Toast.makeText(
                                    TherapistSessionDetailsActivity.this,
                                    "Unable to complete session. Error: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TherapistSessionResponse> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        requestInProgress = false;
                        loadSessionDetails();

                        Toast.makeText(
                                TherapistSessionDetailsActivity.this,
                                "Could not complete session. Check connection.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void showError(String message) {
        tvSessionTherapy.setText("Session unavailable");
        tvSessionChild.setText("N/A");
        tvSessionDate.setText("N/A");
        tvSessionTime.setText("N/A");
        tvSessionRoom.setText("N/A");
        tvSessionStatus.setText("Unable to load");
        tvSessionNote.setText(message);
        btnStartSession.setEnabled(false);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "N/A"
                : value;
    }

    private String formatStatus(String status) {
        switch (status) {
            case "SCHEDULED":
                return "Scheduled";
            case "IN_PROGRESS":
                return "In Progress";
            case "COMPLETED":
                return "Completed";
            case "CANCELLED":
                return "Cancelled";
            default:
                return status.isEmpty() ? "Unknown" : status;
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "SCHEDULED":
                return Color.rgb(40, 116, 178);
            case "IN_PROGRESS":
                return Color.rgb(230, 126, 34);
            case "COMPLETED":
                return Color.rgb(46, 125, 50);
            case "CANCELLED":
                return Color.rgb(198, 40, 40);
            default:
                return Color.rgb(117, 117, 117);
        }
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.therapist_nav_schedule;
    }
}
