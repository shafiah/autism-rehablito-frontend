
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.TherapistChildResponse;
import com.autismcentre.model.TherapistSessionResponse;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistHomeDashboardActivity extends BaseTherapistActivity {

    private TextView tvTherapyType;
    private TextView tvChildName;
    private TextView tvSessionTime;
    private TextView tvAssignedChildren;

    private MaterialButton btnViewSession;

    private ApiService apiService;

    private Call<List<TherapistSessionResponse>> sessionsCall;
    private Call<List<TherapistChildResponse>> childrenCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.content_therapist_home);
        setPageTitle("Rehablito Autism Centre");

        apiService = ApiClient.getApiService();

        tvTherapyType = findViewById(R.id.tvTherapyType);
        tvChildName = findViewById(R.id.tvChildName);
        tvSessionTime = findViewById(R.id.tvSessionTime);
        tvAssignedChildren = findViewById(R.id.tvAssignedChildren);
        btnViewSession = findViewById(R.id.btnViewSession);

        // Load actual dashboard data
        loadTodaySession();
        loadAssignedChildrenCount();

        // View Session navigation preserved
        btnViewSession.setOnClickListener(v ->
                startActivity(new Intent(
                        TherapistHomeDashboardActivity.this,
                        TherapistScheduleActivity.class
                ))
        );

        // View Assigned Children navigation preserved
        MaterialButton btnViewChildren =
                findViewById(R.id.btnViewChildren);

        btnViewChildren.setOnClickListener(v ->
                startActivity(new Intent(
                        TherapistHomeDashboardActivity.this,
                        TherapistChildrenActivity.class
                ))
        );

        // Therapy Reports navigation preserved
        MaterialButton btnTherapyReports =
                findViewById(R.id.btnTherapyReports);

        btnTherapyReports.setOnClickListener(v ->
                startActivity(new Intent(
                        TherapistHomeDashboardActivity.this,
                        TherapistReportsActivity.class
                ))
        );

        // Parent Communication navigation preserved
        MaterialButton btnParentCommunication =
                findViewById(R.id.btnParentCommunication);

        btnParentCommunication.setOnClickListener(v ->
                startActivity(new Intent(
                        TherapistHomeDashboardActivity.this,
                        TherapistParentChatActivity.class
                ))
        );

        // Homework navigation preserved
        MaterialButton btnHomework =
                findViewById(R.id.btnHomework);

        btnHomework.setOnClickListener(v ->
                startActivity(new Intent(
                        TherapistHomeDashboardActivity.this,
                        TherapistHomeworkActivity.class
                ))
        );

        // Parent Feedback navigation
        MaterialButton btnTherapistFeedback =
                findViewById(R.id.btnTherapistFeedback);

        btnTherapistFeedback.setOnClickListener(v ->
                startActivity(new Intent(
                        TherapistHomeDashboardActivity.this,
                        TherapistFeedbackActivity.class
                ))
        );
    }

    private void loadTodaySession() {

        tvTherapyType.setText("Loading session...");
        tvChildName.setText("Loading child details...");
        tvSessionTime.setText("");

        sessionsCall = apiService.getMyTherapistSessions();

        sessionsCall.enqueue(
                new Callback<List<TherapistSessionResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TherapistSessionResponse>> call,
                            Response<List<TherapistSessionResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<TherapistSessionResponse> sessions =
                                    response.body();

                            TherapistSessionResponse todaySession =
                                    findTodaySession(sessions);

                            if (todaySession != null) {
                                displaySession(todaySession);
                            } else {
                                tvTherapyType.setText(
                                        "No session scheduled today"
                                );
                                tvChildName.setText(
                                        "No child session available"
                                );
                                tvSessionTime.setText("");
                            }

                        } else {
                            showSessionError();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistSessionResponse>> call,
                            Throwable throwable) {

                        if (isFinishing() || isDestroyed()
                                || call.isCanceled()) {
                            return;
                        }

                        showSessionError();
                    }
                }
        );
    }

    private TherapistSessionResponse findTodaySession(
            List<TherapistSessionResponse> sessions) {

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        for (TherapistSessionResponse session : sessions) {

            if (session == null || session.getSessionDate() == null) {
                continue;
            }

            String sessionDate = session.getSessionDate();

            // Handle ISO date or ISO date-time values.
            if (sessionDate.length() >= 10
                    && sessionDate.substring(0, 10).equals(today)) {
                return session;
            }

            // Handle dates formatted as dd/MM/yyyy.
            try {
                SimpleDateFormat inputFormat =
                        new SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                        );

                inputFormat.setLenient(false);

                Date parsedDate = inputFormat.parse(sessionDate);

                if (parsedDate != null) {
                    String normalizedDate =
                            new SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.getDefault()
                            ).format(parsedDate);

                    if (normalizedDate.equals(today)) {
                        return session;
                    }
                }
            } catch (ParseException ignored) {
                // Try the next session.
            }
        }

        return null;
    }

    private void displaySession(TherapistSessionResponse session) {

        String therapyType = session.getTherapyType();
        String childName = session.getChildName();

        tvTherapyType.setText(
                therapyType == null || therapyType.trim().isEmpty()
                        ? "Therapy Session"
                        : therapyType
        );

        tvChildName.setText(
                "Child: " +
                        (childName == null || childName.trim().isEmpty()
                                ? "Not available"
                                : childName)
        );

        String startTime = formatTime(session.getStartTime());
        String endTime = formatTime(session.getEndTime());

        if (!startTime.isEmpty() && !endTime.isEmpty()) {
            tvSessionTime.setText(startTime + " - " + endTime);
        } else if (!startTime.isEmpty()) {
            tvSessionTime.setText(startTime);
        } else {
            tvSessionTime.setText("Session time not available");
        }

        btnViewSession.setVisibility(View.VISIBLE);
    }

    private String formatTime(String time) {

        if (time == null || time.trim().isEmpty()) {
            return "";
        }

        // Convert HH:mm:ss or HH:mm to a readable time.
        String[] formats = {"HH:mm:ss", "HH:mm"};

        for (String format : formats) {
            try {
                SimpleDateFormat inputFormat =
                        new SimpleDateFormat(
                                format,
                                Locale.getDefault()
                        );

                inputFormat.setLenient(false);

                SimpleDateFormat outputFormat =
                        new SimpleDateFormat(
                                "hh:mm a",
                                Locale.getDefault()
                        );

                Date parsedTime = inputFormat.parse(time);

                if (parsedTime != null) {
                    return outputFormat.format(parsedTime);
                }
            } catch (ParseException ignored) {
                // Try the next format.
            }
        }

        return time;
    }

    private void showSessionError() {

        tvTherapyType.setText("Unable to load session");
        tvChildName.setText("Please check your connection");
        tvSessionTime.setText("");
    }

    private void loadAssignedChildrenCount() {

        tvAssignedChildren.setText("Loading assigned children...");

        childrenCall = apiService.getMyTherapistChildren();

        childrenCall.enqueue(
                new Callback<List<TherapistChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TherapistChildResponse>> call,
                            Response<List<TherapistChildResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            int count = response.body().size();

                            tvAssignedChildren.setText(
                                    count + (count == 1
                                            ? " Child Assigned"
                                            : " Children Assigned")
                            );

                        } else {
                            tvAssignedChildren.setText(
                                    "Unable to load assigned children"
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistChildResponse>> call,
                            Throwable throwable) {

                        if (isFinishing() || isDestroyed()
                                || call.isCanceled()) {
                            return;
                        }

                        tvAssignedChildren.setText(
                                "Unable to load assigned children"
                        );
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh counts and session after returning to dashboard.
        if (apiService != null && tvAssignedChildren != null) {
            loadAssignedChildrenCount();
        }

        if (apiService != null && tvTherapyType != null) {
            loadTodaySession();
        }
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.therapist_nav_home;
    }

    @Override
    protected void onDestroy() {

        if (sessionsCall != null) {
            sessionsCall.cancel();
        }

        if (childrenCall != null) {
            childrenCall.cancel();
        }

        super.onDestroy();
    }
}
