
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager2.widget.ViewPager2;

import com.autismcentre.R;
import com.autismcentre.adapter.ImageSliderAdapter;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.model.ParentChildTherapistResponse;
import com.autismcentre.repository.ParentChildRepository;
import com.autismcentre.model.ParentSessionResponse;
import com.autismcentre.repository.ParentSessionRepository;
import android.widget.ProgressBar;

import com.autismcentre.model.ProgressRecordResponse;
import com.autismcentre.repository.ParentProgressRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import java.text.ParseException;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentHomeDashboardActivity extends BaseParentActivity {

    private static final String TAG = "ParentHomeDashboard";

    private TextView tvChildName;
    private TextView tvChildAge;
    private TextView tvActiveTherapy;

    private ParentChildRepository repository;

    private Long childId;

    private ParentSessionRepository sessionRepository;

    private TextView tvSessionTherapy;
    private TextView tvSessionStatus;
    private TextView tvSessionTime;
    private TextView tvSessionTherapist;
    private TextView tvSessionRoom;
    private Long selectedSessionId = null;
    // =====================================================
// DASHBOARD PROGRESS
// =====================================================

    private ParentProgressRepository progressRepository;

    private TextView tvOtPercentage;
    private TextView tvSpeechPercentage;

    private ProgressBar progressOt;
    private ProgressBar progressSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =====================================================
        // HOME CONTENT
        // =====================================================

        showPageContent(R.layout.content_parent_home);

        // =====================================================
        // API INITIALIZATION
        // =====================================================

        ApiClient.initialize(getApplicationContext());
        repository = new ParentChildRepository();
        sessionRepository = new ParentSessionRepository();
        // Progress repository
        progressRepository = new ParentProgressRepository();

        // =====================================================
        // MY CHILD VIEWS
        // =====================================================

        tvChildName = findViewById(R.id.tvChildName);
        tvChildAge = findViewById(R.id.tvChildAge);
        tvActiveTherapy = findViewById(R.id.tvActiveTherapy);

        tvChildName.setText("Loading...");
        tvChildAge.setText("Age: Loading...");
        tvActiveTherapy.setText("Loading active therapies...");

        tvSessionTherapy = findViewById(R.id.tvSessionTherapy);
        tvSessionStatus = findViewById(R.id.tvSessionStatus);
        tvSessionTime = findViewById(R.id.tvSessionTime);
        tvSessionTherapist = findViewById(R.id.tvSessionTherapist);
        tvSessionRoom = findViewById(R.id.tvSessionRoom);

        // Initial loading state
        tvSessionTherapy.setText("Loading session...");
        tvSessionStatus.setText("");
        tvSessionTime.setText("");
        tvSessionTherapist.setText("");
        tvSessionRoom.setText("");

        // =====================================================
        // LOAD LINKED CHILD
        // =====================================================

        childId = getIntent().getLongExtra("childId", -1L);

        if (childId > 0) {
            loadChildDetails(childId);
            loadTodaysSession(childId);
            loadDashboardProgress(childId);
        } else {
            showChildUnavailable();
            showNoSession();

            Log.e(TAG, "Invalid childId received from Intent");
        }

        // =====================================================
// PROGRESS SNAPSHOT VIEWS
// =====================================================

        tvOtPercentage = findViewById(R.id.tvOtPercentage);
        tvSpeechPercentage = findViewById(R.id.tvSpeechPercentage);

        progressOt = findViewById(R.id.progressOt);
        progressSpeech = findViewById(R.id.progressSpeech);

// Loading state: Do not show fabricated percentages.
        tvOtPercentage.setText("--");
        tvSpeechPercentage.setText("--");

        progressOt.setProgress(0);
        progressSpeech.setProgress(0);

        // =====================================================
        // HOME IMAGE SLIDERS
        // =====================================================

        ViewPager2 companyImageSlider =
                findViewById(R.id.companyImageSlider);

        ViewPager2 recoverAutismSlider =
                findViewById(R.id.recoverAutismSlider);

        // =====================================================
        // COMPANY IMAGES
        // =====================================================

        int[] companyImages = {

                R.drawable.company_1,
                R.drawable.company_2,
                R.drawable.company_3,
                R.drawable.company_4,
                R.drawable.company_5
        };

        companyImageSlider.setAdapter(
                new ImageSliderAdapter(companyImages)
        );

        // =====================================================
        // AUTISM AWARENESS / RECOVERY IMAGES
        // =====================================================

        int[] autismImages = {

                R.drawable.autism_1,
                R.drawable.autism_2,
                R.drawable.autism_3,
                R.drawable.autism_4,
                R.drawable.autism_5
        };

        recoverAutismSlider.setAdapter(
                new ImageSliderAdapter(autismImages)
        );

        // =====================================================
        // SCHEDULE
        // =====================================================

        findViewById(R.id.btnSchedule).setOnClickListener(v -> {

            startActivity(new Intent(
                    ParentHomeDashboardActivity.this,
                    ScheduleActivity.class
            ));
        });

        // =====================================================
        // REPORTS
        // =====================================================

        findViewById(R.id.btnReports).setOnClickListener(v -> {

            startActivity(new Intent(
                    ParentHomeDashboardActivity.this,
                    ReportsActivity.class
            ));
        });

        // =====================================================
        // PROGRESS
        // =====================================================

        findViewById(R.id.btnProgress).setOnClickListener(v -> {

            startActivity(new Intent(
                    ParentHomeDashboardActivity.this,
                    ProgressActivity.class
            ));
        });

        // =====================================================
        // PAYMENTS
        // =====================================================

        findViewById(R.id.btnPayments).setOnClickListener(v -> {

            startActivity(new Intent(
                    ParentHomeDashboardActivity.this,
                    PaymentsActivity.class
            ));
        });

        // =====================================================
        // CHILD PROFILE
        // =====================================================

        findViewById(R.id.btnViewChildProfile).setOnClickListener(v -> {

            if (childId == null || childId <= 0) {
                Toast.makeText(
                        ParentHomeDashboardActivity.this,
                        "Child details not available. Please login again.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(
                    ParentHomeDashboardActivity.this,
                    ChildProfileActivity.class
            );

            intent.putExtra("childId", childId);

            startActivity(intent);
        });

        // =====================================================
        // OCCUPATIONAL THERAPY - VIEW DETAILS
        // =====================================================

        findViewById(R.id.btnViewSession).setOnClickListener(v -> {

            if (childId == null || childId <= 0
                    || selectedSessionId == null
                    || selectedSessionId <= 0) {

                Toast.makeText(
                        ParentHomeDashboardActivity.this,
                        "Session details not available.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent = new Intent(
                    ParentHomeDashboardActivity.this,
                    SessionDetailsActivity.class
            );

            intent.putExtra("childId", childId);
            intent.putExtra("sessionId", selectedSessionId);

            startActivity(intent);
        });
        // =====================================================
        // VIEW FULL PROGRESS
        // =====================================================

        findViewById(R.id.btnViewFullProgress).setOnClickListener(v -> {

            startActivity(new Intent(
                    ParentHomeDashboardActivity.this,
                    ProgressActivity.class
            ));

        });

        // =====================================================
        // HOMEWORK
        // =====================================================

        findViewById(R.id.btnHomework).setOnClickListener(v -> {

            startActivity(new Intent(
                    ParentHomeDashboardActivity.this,
                    HomeworkActivity.class
            ));
        });

        // =====================================================
        // COMMUNICATION
        // =====================================================

        findViewById(R.id.btnCommunication).setOnClickListener(v -> {

            if (childId == null || childId <= 0) {
                Toast.makeText(
                        ParentHomeDashboardActivity.this,
                        "Child details not available. Please login again.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(
                    ParentHomeDashboardActivity.this,
                    TherapistListActivity.class
            );

            intent.putExtra("childId", childId);

            startActivity(intent);
        });

        // =====================================================
        // FEEDBACK
        // =====================================================

        findViewById(R.id.btnFeedback).setOnClickListener(v -> {

            startActivity(new Intent(
                    ParentHomeDashboardActivity.this,
                    FeedbackActivity.class
            ));
        });
    }

    // =====================================================
// LOAD DASHBOARD PROGRESS
// =====================================================

    private void loadDashboardProgress(Long id) {

        progressRepository.getMyChildProgress(
                id,
                new Callback<List<ProgressRecordResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ProgressRecordResponse>> call,
                            Response<List<ProgressRecordResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<ProgressRecordResponse> records =
                                    response.body();

                            Log.d(
                                    TAG,
                                    "Dashboard progress records loaded: "
                                            + records.size()
                            );

                            displayDashboardProgress(records);

                        } else {

                            Log.e(
                                    TAG,
                                    "Dashboard progress API failed. HTTP: "
                                            + response.code()
                            );

                            showProgressUnavailable();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ProgressRecordResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(
                                TAG,
                                "Dashboard progress request failed",
                                t
                        );

                        showProgressUnavailable();
                    }
                }
        );
    }

    // =====================================================
// DISPLAY DASHBOARD PROGRESS
// =====================================================

    private void displayDashboardProgress(
            List<ProgressRecordResponse> records) {

        // Reset existing snapshot.
        tvOtPercentage.setText("--");
        tvSpeechPercentage.setText("--");

        progressOt.setProgress(0);
        progressSpeech.setProgress(0);

        if (records == null || records.isEmpty()) {

            Log.d(TAG, "No dashboard progress records available.");

            return;
        }

        ProgressRecordResponse latestOt = null;
        ProgressRecordResponse latestSpeech = null;

        // Backend returns latest records first.
        // First matching record is the latest for that therapy.

        for (ProgressRecordResponse record : records) {

            if (record == null || record.getSessionType() == null) {
                continue;
            }

            String therapy = record.getSessionType().trim();

            if (therapy.equalsIgnoreCase("Occupational Therapy")
                    && latestOt == null) {

                latestOt = record;

            } else if (therapy.equalsIgnoreCase("Speech Therapy")
                    && latestSpeech == null) {

                latestSpeech = record;
            }

            if (latestOt != null && latestSpeech != null) {
                break;
            }
        }

        // Occupational Therapy
        if (latestOt != null) {

            updateTherapyProgress(
                    latestOt.getProgress(),
                    tvOtPercentage,
                    progressOt
            );

        } else {

            Log.d(TAG, "No Occupational Therapy progress record found.");
        }

        // Speech Therapy
        if (latestSpeech != null) {

            updateTherapyProgress(
                    latestSpeech.getProgress(),
                    tvSpeechPercentage,
                    progressSpeech
            );

        } else {

            Log.d(TAG, "No Speech Therapy progress record found.");
        }
    }

    // =====================================================
// UPDATE THERAPY PROGRESS
// =====================================================

    private void updateTherapyProgress(
            String progressValue,
            TextView percentageView,
            ProgressBar progressBar) {

        Integer percentage = parseDashboardPercentage(progressValue);

        if (percentage == null) {

            percentageView.setText("--");
            progressBar.setProgress(0);

            Log.w(
                    TAG,
                    "Invalid progress percentage: " + progressValue
            );

            return;
        }

        percentageView.setText(percentage + "%");
        progressBar.setMax(100);
        progressBar.setProgress(percentage);

        Log.d(
                TAG,
                "Dashboard therapy progress updated: " + percentage + "%"
        );
    }

    // =====================================================
// PARSE DASHBOARD PERCENTAGE
// =====================================================

    private Integer parseDashboardPercentage(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String trimmed = value.trim();

        if (!trimmed.matches("\\d{1,3}\\s*%")) {
            return null;
        }

        try {

            int percentage = Integer.parseInt(
                    trimmed.replace("%", "").trim()
            );

            if (percentage >= 0 && percentage <= 100) {
                return percentage;
            }

        } catch (NumberFormatException e) {

            Log.e(TAG, "Invalid progress percentage: " + value, e);
        }

        return null;
    }

    // =====================================================
// PROGRESS UNAVAILABLE
// =====================================================

    private void showProgressUnavailable() {

        tvOtPercentage.setText("--");
        tvSpeechPercentage.setText("--");

        progressOt.setProgress(0);
        progressSpeech.setProgress(0);
    }

    // =====================================================
    // LOAD CHILD DETAILS
    // =====================================================

    private void loadChildDetails(Long id) {

        repository.getMyChildById(
                id,
                new Callback<ParentChildResponse>() {

                    @Override
                    public void onResponse(
                            Call<ParentChildResponse> call,
                            Response<ParentChildResponse> response
                    ) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            ParentChildResponse child = response.body();

                            tvChildName.setText(
                                    safeValue(
                                            child.getChildName(),
                                            "Name not available"
                                    )
                            );

                            String age = calculateAge(
                                    child.getDateOfBirth()
                            );

                            tvChildAge.setText("Age: " + age);

                            Log.d(
                                    TAG,
                                    "Child details loaded successfully. Child ID: " + id
                            );

                            loadActiveTherapies(id);

                        } else {

                            Log.e(
                                    TAG,
                                    "Child details API failed. HTTP: "
                                            + response.code()
                            );

                            showChildUnavailable();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ParentChildResponse> call,
                            Throwable t
                    ) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(TAG, "Child details request failed", t);

                        showChildUnavailable();
                    }
                }
        );
    }

    // =====================================================
    // LOAD ACTIVE THERAPIES
    // =====================================================

    private void loadActiveTherapies(Long id) {

        repository.getMyChildTherapists(
                id,
                new Callback<List<ParentChildTherapistResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentChildTherapistResponse>> call,
                            Response<List<ParentChildTherapistResponse>> response
                    ) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            int activeCount = 0;

                            for (ParentChildTherapistResponse therapist
                                    : response.body()) {

                                if (therapist != null
                                        && Boolean.TRUE.equals(
                                        therapist.getActive())) {

                                    activeCount++;
                                }
                            }

                            tvActiveTherapy.setText(
                                    "Active Therapies: " + activeCount
                            );

                            Log.d(
                                    TAG,
                                    "Active therapies loaded: " + activeCount
                            );

                        } else {

                            Log.e(
                                    TAG,
                                    "Therapist API failed. HTTP: "
                                            + response.code()
                            );

                            tvActiveTherapy.setText(
                                    "Active Therapies: Not available"
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentChildTherapistResponse>> call,
                            Throwable t
                    ) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(TAG, "Therapist API request failed", t);

                        tvActiveTherapy.setText(
                                "Active Therapies: Not available"
                        );
                    }
                }
        );
    }

    // =====================================================
    // CHILD UNAVAILABLE
    // =====================================================

    private void showChildUnavailable() {

        childId = null;

        tvChildName.setText("Child details unavailable");
        tvChildAge.setText("Age: Not available");
        tvActiveTherapy.setText("Active Therapies: Not available");
    }

    // =====================================================
    // SAFE STRING VALUE
    // =====================================================

    private String safeValue(String value, String fallback) {

        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    // =====================================================
    // CALCULATE AGE FROM DATE OF BIRTH
    // =====================================================

    private String calculateAge(String dob) {

        if (dob == null || dob.trim().isEmpty()) {
            return "Not available";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        );

        sdf.setLenient(false);

        try {

            Date birthDate = sdf.parse(dob);

            if (birthDate == null) {
                return "Not available";
            }

            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);

            Calendar today = Calendar.getInstance();

            if (birth.after(today)) {
                return "Not available";
            }

            int years = today.get(Calendar.YEAR)
                    - birth.get(Calendar.YEAR);

            int months = today.get(Calendar.MONTH)
                    - birth.get(Calendar.MONTH);

            int days = today.get(Calendar.DAY_OF_MONTH)
                    - birth.get(Calendar.DAY_OF_MONTH);

            if (days < 0) {
                months--;
            }

            if (months < 0) {
                years--;
                months += 12;
            }

            if (years < 0) {
                return "Not available";
            }

            if (years == 0) {
                return months + " Months";
            }

            if (months == 0) {
                return years + (years == 1 ? " Year" : " Years");
            }

            return years
                    + (years == 1 ? " Year " : " Years ")
                    + months
                    + " Months";

        } catch (ParseException e) {

            Log.e(TAG, "Invalid DOB format: " + dob, e);

            return "Not available";
        }
    }


// =====================================================
// LOAD TODAY'S SESSION
// =====================================================

    private void loadTodaysSession(Long id) {

        sessionRepository.getMyChildSessions(
                id,
                new Callback<List<ParentSessionResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentSessionResponse>> call,
                            Response<List<ParentSessionResponse>> response
                    ) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            ParentSessionResponse todaySession =
                                    findTodaysSession(response.body());

                            if (todaySession != null) {
                                displayTodaysSession(todaySession);
                            } else {
                                showNoSession();
                            }

                        } else {

                            Log.e(
                                    TAG,
                                    "Sessions API failed. HTTP: "
                                            + response.code()
                            );

                            showSessionUnavailable();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentSessionResponse>> call,
                            Throwable t
                    ) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(TAG, "Sessions API request failed", t);

                        showSessionUnavailable();
                    }
                }
        );
    }

// =====================================================
// FIND ONE SESSION FOR TODAY
// =====================================================

    private ParentSessionResponse findTodaysSession(
            List<ParentSessionResponse> sessions) {

        List<ParentSessionResponse> todaySessions =
                new ArrayList<>();

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        dateFormat.setLenient(false);

        String today = dateFormat.format(new Date());

        for (ParentSessionResponse session : sessions) {

            if (session == null
                    || session.getSessionDate() == null) {
                continue;
            }

            String sessionDate = session.getSessionDate().trim();

            // Handle ISO date-time values too.
            if (sessionDate.length() >= 10) {
                sessionDate = sessionDate.substring(0, 10);
            }

            if (today.equals(sessionDate)) {
                todaySessions.add(session);
            }
        }

        if (todaySessions.isEmpty()) {
            return null;
        }

        int currentMinutes = Calendar.getInstance().get(
                Calendar.HOUR_OF_DAY
        ) * 60 + Calendar.getInstance().get(
                Calendar.MINUTE
        );

        ParentSessionResponse nextSession = null;
        int earliestUpcomingTime = Integer.MAX_VALUE;

        ParentSessionResponse latestSession = null;
        int latestSessionTime = Integer.MIN_VALUE;

        for (ParentSessionResponse session : todaySessions) {

            int startMinutes = getTimeInMinutes(
                    session.getStartTime()
            );

            if (startMinutes < 0) {
                continue;
            }

            if (startMinutes > latestSessionTime) {
                latestSessionTime = startMinutes;
                latestSession = session;
            }

            if (startMinutes >= currentMinutes
                    && startMinutes < earliestUpcomingTime) {

                earliestUpcomingTime = startMinutes;
                nextSession = session;
            }
        }

        // Prefer the next upcoming session.
        // If all today's sessions have started, show the latest one.
        return nextSession != null ? nextSession : latestSession;
    }

// =====================================================
// CONVERT SESSION TIME TO MINUTES
// =====================================================

    private int getTimeInMinutes(String time) {

        if (time == null || time.trim().isEmpty()) {
            return -1;
        }

        String[] formats = {
                "HH:mm:ss",
                "HH:mm",
                "hh:mm a",
                "h:mm a"
        };

        for (String format : formats) {

            try {

                SimpleDateFormat sdf =
                        new SimpleDateFormat(format, Locale.US);

                sdf.setLenient(false);

                Date parsed = sdf.parse(time.trim());

                if (parsed != null) {

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(parsed);

                    return calendar.get(Calendar.HOUR_OF_DAY) * 60
                            + calendar.get(Calendar.MINUTE);
                }

            } catch (ParseException ignored) {
                // Try the next supported format.
            }
        }

        Log.w(TAG, "Unsupported session time format: " + time);

        return -1;
    }

// =====================================================
// DISPLAY TODAY'S SESSION
// =====================================================

    private void displayTodaysSession(ParentSessionResponse session) {

        selectedSessionId = session.getSessionId();

        // Existing code below remains unchanged
        tvSessionTherapy.setText(
                safeValue(
                        session.getTherapyType(),
                        "Therapy not available"
                )
        );

        tvSessionStatus.setText(
                safeValue(
                        session.getStatus(),
                        "Status not available"
                )
        );

        String startTime = safeValue(
                session.getStartTime(),
                ""
        );

        String endTime = safeValue(
                session.getEndTime(),
                ""
        );

        if (!startTime.isEmpty() && !endTime.isEmpty()) {

            tvSessionTime.setText(
                    formatSessionTime(startTime)
                            + " - "
                            + formatSessionTime(endTime)
            );

        } else {
            tvSessionTime.setText("Time not available");
        }

        tvSessionTherapist.setText(
                "Therapist: "
                        + safeValue(
                        session.getTherapistName(),
                        "Not available"
                )
        );

        tvSessionRoom.setText(
                "Branch: "
                        + safeValue(
                        session.getBranchName(),
                        "Not available"
                )
        );

        Log.d(
                TAG,
                "Today's session displayed. Session ID: "
                        + session.getSessionId()
        );
    }

// =====================================================
// FORMAT SESSION TIME
// =====================================================

    private String formatSessionTime(String time) {

        String[] formats = {
                "HH:mm:ss",
                "HH:mm",
                "hh:mm a",
                "h:mm a"
        };

        for (String format : formats) {

            try {

                SimpleDateFormat input =
                        new SimpleDateFormat(format, Locale.US);

                input.setLenient(false);

                Date parsed = input.parse(time.trim());

                if (parsed != null) {

                    SimpleDateFormat output =
                            new SimpleDateFormat(
                                    "hh:mm a",
                                    Locale.US
                            );

                    return output.format(parsed);
                }

            } catch (ParseException ignored) {
                // Try next format.
            }
        }

        return time;
    }

// =====================================================
// NO SESSION TODAY
// =====================================================

    private void showNoSession() {

        tvSessionTherapy.setText(
                "No sessions scheduled for today"
        );

        tvSessionStatus.setText("");
        tvSessionTime.setText("");
        tvSessionTherapist.setText("");
        tvSessionRoom.setText("");
    }

// =====================================================
// SESSION API UNAVAILABLE
// =====================================================

    private void showSessionUnavailable() {

        tvSessionTherapy.setText(
                "Session details unavailable"
        );

        tvSessionStatus.setText("");
        tvSessionTime.setText("");
        tvSessionTherapist.setText("");
        tvSessionRoom.setText("");
    }


    // =====================================================
    // CURRENT BOTTOM NAVIGATION ITEM
    // =====================================================

    @Override
    protected int getCurrentBottomNavItem() {

        return R.id.nav_home;
    }
}
