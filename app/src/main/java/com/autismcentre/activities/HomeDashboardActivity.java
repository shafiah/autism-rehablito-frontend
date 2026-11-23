
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.model.ParentChildTherapistResponse;
import com.autismcentre.repository.ParentChildRepository;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeDashboardActivity extends AppCompatActivity {

    private static final String TAG = "HomeDashboard";

    private MaterialButton btnViewChildProfile;
    private MaterialButton btnSchedule;
    private MaterialButton btnHomework;
    private MaterialButton btnCommunication;
    private MaterialButton btnFeedback;

    private TextView tvChildName;
    private TextView tvChildAge;
    private TextView tvActiveTherapy;

    private ParentChildRepository repository;
    private Long childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dashboard);

        ApiClient.initialize(getApplicationContext());
        repository = new ParentChildRepository();

        initializeViews();
        setupClickListeners();
        loadMyChild();
    }

    private void initializeViews() {

        tvChildName = findViewById(R.id.tvChildName);
        tvChildAge = findViewById(R.id.tvChildAge);
        tvActiveTherapy = findViewById(R.id.tvActiveTherapy);
    }

    private void setupClickListeners() {

        btnFeedback = findViewById(R.id.btnFeedback);

        btnFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeDashboardActivity.this,
                    FeedbackActivity.class
            );
            startActivity(intent);
        });

        btnCommunication = findViewById(R.id.btnCommunication);

        btnCommunication.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeDashboardActivity.this,
                    TherapistListActivity.class
            );
            startActivity(intent);
        });

        btnHomework = findViewById(R.id.btnHomework);

        btnHomework.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeDashboardActivity.this,
                    HomeworkActivity.class
            );
            startActivity(intent);
        });

        MaterialButton btnPayments = findViewById(R.id.btnPayments);

        btnPayments.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeDashboardActivity.this,
                    PaymentsActivity.class
            );
            startActivity(intent);
        });

        MaterialButton btnProgress = findViewById(R.id.btnProgress);

        btnProgress.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeDashboardActivity.this,
                    ProgressActivity.class
            );
            startActivity(intent);
        });

        MaterialButton btnReports = findViewById(R.id.btnReports);

        btnReports.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeDashboardActivity.this,
                    ReportsActivity.class
            );
            startActivity(intent);
        });

        btnSchedule = findViewById(R.id.btnSchedule);

        btnSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeDashboardActivity.this,
                    ScheduleActivity.class
            );
            startActivity(intent);
        });

        btnViewChildProfile = findViewById(R.id.btnViewChildProfile);

        btnViewChildProfile.setOnClickListener(v -> {

            if (childId == null || childId <= 0) {
                Toast.makeText(
                        this,
                        "Child details not available.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(
                    HomeDashboardActivity.this,
                    ChildProfileActivity.class
            );

            intent.putExtra("childId", childId);
            startActivity(intent);
        });
    }

    // Load currently linked child
    private void loadMyChild() {

        tvChildName.setText("Loading...");
        tvChildAge.setText("Age: Loading...");
        tvActiveTherapy.setText("Loading active therapies...");

        repository.getMyChildren(
                new Callback<List<ParentChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentChildResponse>> call,
                            Response<List<ParentChildResponse>> response
                    ) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {

                            ParentChildResponse child =
                                    response.body().get(0);

                            childId = child.getChildId();

                            if (childId == null || childId <= 0) {
                                showChildUnavailable();
                                return;
                            }

                            loadChildDetails(childId);
                            Log.d(TAG, "Children API HTTP: " + response.code());
                            Log.d(TAG, "Children API Body: " + new Gson().toJson(response.body()));

                        } else {
                            Log.e(
                                    TAG,
                                    "Children API failed: HTTP "
                                            + response.code()
                            );

                            showChildUnavailable();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentChildResponse>> call,
                            Throwable t
                    ) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(TAG, "Children API error", t);
                        showChildUnavailable();
                    }
                }
        );
    }

    // Load child profile details
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
                                            "Child"
                                    )
                            );

                            tvChildAge.setText(
                                    "Age: " + calculateAge(
                                            child.getDateOfBirth()
                                    )
                            );

                            loadActiveTherapies(id);
                            Log.d(TAG, "Child Name: " + response.body().getChildName());
                            Log.d(TAG, "Child DOB: " + response.body().getDateOfBirth());

                        } else {
                            Log.e(
                                    TAG,
                                    "Child details API failed: HTTP "
                                            + response.code()
                            );

                            tvChildName.setText("Unable to load child");
                            tvChildAge.setText("Age: Not available");
                            tvActiveTherapy.setText(
                                    "Unable to load therapies"
                            );
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

                        Log.e(TAG, "Child details error", t);

                        tvChildName.setText("Unable to load child");
                        tvChildAge.setText("Age: Not available");
                        tvActiveTherapy.setText(
                                "Unable to load therapies"
                        );
                    }
                }
        );
    }

    // Load actual active therapy count
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

                                if (Boolean.TRUE.equals(
                                        therapist.getActive())) {
                                    activeCount++;
                                }
                            }

                            tvActiveTherapy.setText(
                                    "Active Therapies: " + activeCount
                            );

                        } else {
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

                        Log.e(TAG, "Therapies API error", t);

                        tvActiveTherapy.setText(
                                "Active Therapies: Not available"
                        );
                    }
                }
        );
    }

    private void showChildUnavailable() {

        childId = null;

        tvChildName.setText("No child linked");
        tvChildAge.setText("Age: Not available");
        tvActiveTherapy.setText("Active Therapies: 0");
    }

    private String safeValue(String value, String fallback) {

        return value != null && !value.trim().isEmpty()
                ? value
                : fallback;
    }

    private String calculateAge(String dob) {

        if (dob == null || dob.trim().isEmpty()) {
            return "Not available";
        }

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        dateFormat.setLenient(false);

        try {

            Date birthDate = dateFormat.parse(dob);

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

                Calendar previousMonth =
                        (Calendar) today.clone();

                previousMonth.add(Calendar.MONTH, -1);

                days += previousMonth.getActualMaximum(
                        Calendar.DAY_OF_MONTH
                );
            }

            if (months < 0) {
                years--;
                months += 12;
            }

            if (years < 0) {
                return "Not available";
            }

            if (years == 0 && months == 0) {
                return days + (days == 1 ? " day" : " days");
            }

            if (years == 0) {
                return months
                        + (months == 1 ? " month" : " months");
            }

            if (months == 0) {
                return years
                        + (years == 1 ? " year" : " years");
            }

            return years
                    + (years == 1 ? " year " : " years ")
                    + months
                    + (months == 1 ? " month" : " months");

        } catch (ParseException e) {
            Log.e(TAG, "Age calculation failed", e);
            return "Not available";
        }
    }
}
