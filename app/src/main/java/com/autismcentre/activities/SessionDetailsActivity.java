
package com.autismcentre.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentSessionResponse;
import com.autismcentre.repository.ParentSessionRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SessionDetailsActivity extends BaseParentActivity {

    private Long childId;
    private Long sessionId;

    private TextView tvSessionTitle;
    private TextView tvSessionDate;
    private TextView tvSessionTime;
    private TextView tvTherapist;
    private TextView tvBranch;
    private TextView tvSessionStatus;
    private TextView tvRemarks;

    private LinearLayout sessionDetailsContainer;

    private ParentSessionRepository sessionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_session_details);

        ApiClient.initialize(getApplicationContext());

        childId = getIntent().hasExtra("childId")
                ? getIntent().getLongExtra("childId", -1L)
                : null;

        sessionId = getIntent().hasExtra("sessionId")
                ? getIntent().getLongExtra("sessionId", -1L)
                : null;

        sessionRepository = new ParentSessionRepository();

        /*
         * The XML IDs listed below must exist in activity_session_details.xml.
         * If your layout uses different IDs, share that XML before changing it.
         */
        tvSessionTitle = findViewById(R.id.tvSessionTitle);
        tvSessionDate = findViewById(R.id.tvSessionDate);
        tvSessionTime = findViewById(R.id.tvSessionTime);
        tvTherapist = findViewById(R.id.tvTherapist);
        tvBranch = findViewById(R.id.tvBranch);
        tvSessionStatus = findViewById(R.id.tvSessionStatus);
        tvRemarks = findViewById(R.id.tvRemarks);

        if (childId == null || childId <= 0
                || sessionId == null || sessionId <= 0) {
            showMessage("Session information is missing.");
            return;
        }

        loadSessionDetails();
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.nav_schedule;
    }

    private void loadSessionDetails() {
        sessionRepository.getMyChildSessionById(
                childId,
                sessionId,
                new Callback<ParentSessionResponse>() {
                    @Override
                    public void onResponse(
                            Call<ParentSessionResponse> call,
                            Response<ParentSessionResponse> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage("Unable to load session details.");
                            return;
                        }

                        displaySession(response.body());
                    }

                    @Override
                    public void onFailure(
                            Call<ParentSessionResponse> call,
                            Throwable t) {
                        showMessage("Network error while loading session details.");
                    }
                });
    }

    private void displaySession(ParentSessionResponse session) {
        tvSessionTitle.setText(valueOrDefault(
                session.getTherapyType(), "Therapy Session"));

        tvSessionDate.setText("Date: " + valueOrDefault(
                session.getSessionDate(), "Not specified"));

        tvSessionTime.setText(
                "Time: " + formatTime(session.getStartTime())
                        + " - " + formatTime(session.getEndTime()));

        tvTherapist.setText("Therapist: " + valueOrDefault(
                session.getTherapistName(), "Not assigned"));

        tvBranch.setText("Branch: " + valueOrDefault(
                session.getBranchName(), "Not specified"));

        tvSessionStatus.setText("Status: " + valueOrDefault(
                session.getStatus(), "Not specified"));

        tvRemarks.setText("Remarks: " + valueOrDefault(
                session.getRemarks(), "No additional remarks"));
    }

    private String formatTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return "Not specified";
        }

        return time.length() >= 5 ? time.substring(0, 5) : time;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value;
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
