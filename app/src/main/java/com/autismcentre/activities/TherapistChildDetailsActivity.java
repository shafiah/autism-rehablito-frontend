
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
import com.autismcentre.model.TherapistProgressResponse;
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

public class TherapistChildDetailsActivity extends BaseTherapistActivity {

    private TextView tvChildName;
    private TextView tvChildAge;
    private TextView tvParentName;
    private TextView tvTherapyType;
    private TextView tvOverallProgress;
    private TextView tvCurrentGoals;
    private TextView tvUpcomingSession;

    private MaterialButton btnViewReports;
    private MaterialButton btnAddProgress;
    private MaterialButton btnViewHomework;

    private ApiService apiService;

    private Long childId;
    private String childName = "Child";

    private Call<TherapistChildResponse> childCall;
    private Call<List<TherapistProgressResponse>> progressCall;
    private Call<List<TherapistSessionResponse>> sessionsCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageTitle("Rehablito Autism Center");
        showPageContent(R.layout.activity_therapist_child_details);

        apiService = ApiClient.getApiService();

        tvChildName = findViewById(R.id.tvChildName);
        tvChildAge = findViewById(R.id.tvChildAge);
        tvParentName = findViewById(R.id.tvParentName);
        tvTherapyType = findViewById(R.id.tvTherapyType);
        tvOverallProgress = findViewById(R.id.tvOverallProgress);
        tvCurrentGoals = findViewById(R.id.tvCurrentGoals);
        tvUpcomingSession = findViewById(R.id.tvUpcomingSession);

        btnViewReports = findViewById(R.id.btnViewReports);
        btnAddProgress = findViewById(R.id.btnAddProgress);
        btnViewHomework = findViewById(R.id.btnViewHomework);

        childId = getIntent().hasExtra("child_id")
                ? getIntent().getLongExtra("child_id", -1L)
                : -1L;

        String receivedName = getIntent().getStringExtra("child_name");

        if (receivedName != null && !receivedName.trim().isEmpty()) {
            childName = receivedName;
        }

        tvChildName.setText(childName);
        tvChildAge.setText("Age: Loading...");
        tvParentName.setText("Parent: Not available");
        tvTherapyType.setText("Loading assigned therapy...");
        tvOverallProgress.setText("Loading progress...");
        tvCurrentGoals.setText("Loading current goals...");
        tvUpcomingSession.setText("Loading upcoming session...");

        if (childId == null || childId <= 0) {
            showChildLoadError();
        } else {
            loadChildDetails();
            loadChildProgress();
            loadChildSessions();
        }

        btnViewReports.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TherapistChildDetailsActivity.this,
                    TherapistChildReportActivity.class
            );

            passChildInformation(intent);
            startActivity(intent);
        });

        btnAddProgress.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TherapistChildDetailsActivity.this,
                    TherapistChildReportActivity.class
            );

            passChildInformation(intent);
            intent.putExtra("open_progress_dialog", true);
            startActivity(intent);
        });

        btnViewHomework.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TherapistChildDetailsActivity.this,
                    TherapistHomeworkActivity.class
            );

            intent.putExtra("child_name", childName);

            if (childId != null && childId > 0) {
                intent.putExtra("child_id", childId);
            }

            startActivity(intent);
        });
    }

    private void loadChildDetails() {

        childCall = apiService.getMyTherapistChildById(childId);

        childCall.enqueue(new Callback<TherapistChildResponse>() {
            @Override
            public void onResponse(
                    Call<TherapistChildResponse> call,
                    Response<TherapistChildResponse> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    showChildLoadError();
                    return;
                }

                TherapistChildResponse child = response.body();

                if (child.getChildName() != null
                        && !child.getChildName().trim().isEmpty()) {
                    childName = child.getChildName();
                }

                tvChildName.setText(childName);
                tvChildAge.setText(
                        "Age: " + calculateAge(child.getDateOfBirth())
                );

                // Current child API does not return the parent's name.
                String parentName = child.getParentName();

                if (parentName != null && !parentName.trim().isEmpty()) {
                    tvParentName.setText("Parent: " + parentName);
                } else {
                    tvParentName.setText("Parent: Not available");
                }
            }

            @Override
            public void onFailure(
                    Call<TherapistChildResponse> call,
                    Throwable throwable) {

                if (isFinishing() || isDestroyed() || call.isCanceled()) {
                    return;
                }

                showChildLoadError();
            }
        });
    }

    private void loadChildProgress() {

        progressCall = apiService.getTherapistChildProgress(childId);

        progressCall.enqueue(
                new Callback<List<TherapistProgressResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<TherapistProgressResponse>> call,
                            Response<List<TherapistProgressResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            tvOverallProgress.setText(
                                    "Unable to load progress"
                            );
                            tvCurrentGoals.setText(
                                    "Unable to load current goals"
                            );
                            return;
                        }

                        List<TherapistProgressResponse> records =
                                response.body();

                        if (records.isEmpty()) {
                            tvOverallProgress.setText(
                                    "Progress not recorded yet"
                            );
                            tvCurrentGoals.setText(
                                    "No goals recorded yet."
                            );
                            return;
                        }

                        TherapistProgressResponse latest = records.get(0);

                        String progress = latest.getProgress();
                        String goals = latest.getGoals();

                        tvOverallProgress.setText(
                                progress == null || progress.trim().isEmpty()
                                        ? "Progress not recorded"
                                        : progress
                        );

                        tvCurrentGoals.setText(
                                goals == null || goals.trim().isEmpty()
                                        ? "No goals recorded."
                                        : goals
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistProgressResponse>> call,
                            Throwable throwable) {

                        if (isFinishing() || isDestroyed() || call.isCanceled()) {
                            return;
                        }

                        tvOverallProgress.setText(
                                "Unable to load progress"
                        );
                        tvCurrentGoals.setText(
                                "Unable to load current goals"
                        );
                    }
                }
        );
    }

    private void loadChildSessions() {

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

                        if (!response.isSuccessful() || response.body() == null) {
                            tvTherapyType.setText(
                                    "Unable to load assigned therapy"
                            );
                            tvUpcomingSession.setText(
                                    "Unable to load session details"
                            );
                            return;
                        }

                        TherapistSessionResponse nextSession =
                                findNextChildSession(response.body());

                        if (nextSession == null) {
                            tvTherapyType.setText(
                                    "No assigned session found"
                            );
                            tvUpcomingSession.setText(
                                    "No upcoming session available"
                            );
                            return;
                        }

                        String therapyType = nextSession.getTherapyType();

                        tvTherapyType.setText(
                                therapyType == null || therapyType.trim().isEmpty()
                                        ? "Therapy not available"
                                        : therapyType
                        );

                        tvUpcomingSession.setText(
                                formatSession(nextSession)
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistSessionResponse>> call,
                            Throwable throwable) {

                        if (isFinishing() || isDestroyed() || call.isCanceled()) {
                            return;
                        }

                        tvTherapyType.setText(
                                "Unable to load assigned therapy"
                        );
                        tvUpcomingSession.setText(
                                "Unable to load session details"
                        );
                    }
                }
        );
    }


    private TherapistSessionResponse findNextChildSession(
            List<TherapistSessionResponse> sessions) {

        TherapistSessionResponse inProgressSession = null;
        TherapistSessionResponse upcomingSession = null;
        TherapistSessionResponse latestCompletedSession = null;

        Date now = new Date();
        Date upcomingDateTime = null;
        Date latestCompletedDateTime = null;

        for (TherapistSessionResponse session : sessions) {

            if (session == null
                    || session.getChildId() == null
                    || !childId.equals(session.getChildId())) {
                continue;
            }

            String status = session.getStatus();

            if ("CANCELLED".equalsIgnoreCase(status)) {
                continue;
            }

            // Prefer an active session, even if its scheduled start time passed.
            if ("IN_PROGRESS".equalsIgnoreCase(status)) {
                inProgressSession = session;
                break;
            }

            Date sessionDateTime = parseSessionDateTime(session);

            if ("COMPLETED".equalsIgnoreCase(status)) {
                if (sessionDateTime != null
                        && (latestCompletedDateTime == null
                        || sessionDateTime.after(latestCompletedDateTime))) {

                    latestCompletedDateTime = sessionDateTime;
                    latestCompletedSession = session;
                }
                continue;
            }

            // Keep the nearest future session.
            if (sessionDateTime != null
                    && !sessionDateTime.before(now)
                    && (upcomingDateTime == null
                    || sessionDateTime.before(upcomingDateTime))) {

                upcomingDateTime = sessionDateTime;
                upcomingSession = session;
            }
        }

        if (inProgressSession != null) {
            return inProgressSession;
        }

        if (upcomingSession != null) {
            return upcomingSession;
        }

        // If no active or upcoming session exists, show the latest completed one.
        return latestCompletedSession;
    }

    private Date parseSessionDateTime(TherapistSessionResponse session) {

        String date = session.getSessionDate();
        String time = session.getStartTime();

        if (date == null || time == null) {
            return null;
        }

        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf =
                        new SimpleDateFormat(pattern, Locale.getDefault());

                sdf.setLenient(false);

                Date parsed = sdf.parse(date + " " + time);

                if (parsed != null) {
                    return parsed;
                }
            } catch (ParseException ignored) {
                // Try another supported format.
            }
        }

        return null;
    }

    private String formatSession(TherapistSessionResponse session) {

        String date = session.getSessionDate() == null
                ? ""
                : session.getSessionDate();

        String start = formatTime(session.getStartTime());
        String end = formatTime(session.getEndTime());

        StringBuilder result = new StringBuilder();

        if (!date.isEmpty()) {
            result.append(date);
        }

        if (!start.isEmpty() || !end.isEmpty()) {
            if (result.length() > 0) {
                result.append(" • ");
            }

            result.append(start.isEmpty() ? "Time not available" : start);

            if (!end.isEmpty()) {
                result.append(" - ").append(end);
            }
        }

        String status = session.getStatus();

        if (status != null && !status.trim().isEmpty()) {
            if (result.length() > 0) {
                result.append("\n");
            }

            result.append("Status: ").append(status);
        }

        return result.length() == 0
                ? "Session details not available"
                : result.toString();
    }

    private String formatTime(String time) {

        if (time == null || time.trim().isEmpty()) {
            return "";
        }

        String[] patterns = {"HH:mm:ss", "HH:mm"};

        for (String pattern : patterns) {
            try {
                SimpleDateFormat input =
                        new SimpleDateFormat(pattern, Locale.getDefault());

                input.setLenient(false);

                Date parsed = input.parse(time);

                if (parsed != null) {
                    return new SimpleDateFormat(
                            "hh:mm a",
                            Locale.getDefault()
                    ).format(parsed);
                }
            } catch (ParseException ignored) {
                // Try next pattern.
            }
        }

        return time;
    }

    private String calculateAge(String dateOfBirth) {

        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            return "Not available";
        }

        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            sdf.setLenient(false);

            Date dob = sdf.parse(dateOfBirth);

            if (dob == null || dob.after(new Date())) {
                return "Not available";
            }

            java.util.Calendar birth = java.util.Calendar.getInstance();
            birth.setTime(dob);

            java.util.Calendar today = java.util.Calendar.getInstance();

            int years = today.get(java.util.Calendar.YEAR)
                    - birth.get(java.util.Calendar.YEAR);

            int currentDay = today.get(java.util.Calendar.DAY_OF_YEAR);
            int birthDay = birth.get(java.util.Calendar.DAY_OF_YEAR);

            if (currentDay < birthDay) {
                years--;
            }

            return years + (years == 1 ? " Year" : " Years");

        } catch (ParseException exception) {
            return "Not available";
        }
    }

    private void showChildLoadError() {
        tvChildName.setText("Unable to load child");
        tvChildAge.setText("Age: Not available");
    }

    private void passChildInformation(Intent intent) {

        intent.putExtra("child_name", childName);

        if (childId != null && childId > 0) {
            intent.putExtra("child_id", childId);
        }
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.therapist_nav_children;
    }

    @Override
    protected void onDestroy() {

        if (childCall != null) {
            childCall.cancel();
        }

        if (progressCall != null) {
            progressCall.cancel();
        }

        if (sessionsCall != null) {
            sessionsCall.cancel();
        }

        super.onDestroy();
    }
}
