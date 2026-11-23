
package com.autismcentre.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.model.AdminSessionResponse;
import com.autismcentre.model.AdminSessionUpdateRequest;
import com.autismcentre.repository.AdminSessionRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminEditSessionActivity extends BaseAdminActivity {

    private MaterialAutoCompleteTextView actBranch, actChild,
            actTherapist, actTherapy, actStatus;

    private TextInputEditText etDate, etTime, etEndTime, etRemarks;
    private MaterialButton btnSave;

    private AdminSessionRepository sessionRepository;
    private AdminSessionResponse currentSession;

    private Long sessionId;
    private boolean requestInProgress = false;

    private String sessionDateForApi = "";
    private String startTimeForApi = "";
    private String endTimeForApi = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");
        showAdminPageContent(R.layout.activity_admin_edit_session);

        initializeViews();

        sessionRepository = new AdminSessionRepository();

        sessionId = getIntent().getLongExtra("session_id", -1L);

        setupPickers();
        setupSaveButton();
        makeReadOnlyFields();

        if (sessionId <= 0) {
            showToast("Session ID nahi mili. Details screen se dobara open karo.");
            btnSave.setEnabled(false);
            return;
        }

        loadExistingSessionData();
    }

    private void initializeViews() {
        actBranch = findViewById(R.id.actAdminEditSessionBranch);
        actChild = findViewById(R.id.actAdminEditSessionChild);
        actTherapist = findViewById(R.id.actAdminEditSessionTherapist);
        actTherapy = findViewById(R.id.actAdminEditSessionTherapy);
        actStatus = findViewById(R.id.actAdminEditSessionStatus);

        etDate = findViewById(R.id.etAdminEditSessionDate);
        etTime = findViewById(R.id.etAdminEditSessionTime);
        etEndTime = findViewById(R.id.etAdminEditSessionEndTime);
        etRemarks = findViewById(R.id.etAdminEditSessionRemarks);

        btnSave = findViewById(R.id.btnAdminSaveSessionChanges);
    }

    private void makeReadOnlyFields() {
        actBranch.setKeyListener(null);
        actBranch.setFocusable(false);
        actBranch.setOnClickListener(null);

        actChild.setKeyListener(null);
        actChild.setFocusable(false);
        actChild.setOnClickListener(null);

        actTherapist.setKeyListener(null);
        actTherapist.setFocusable(false);
        actTherapist.setOnClickListener(null);

        actTherapy.setKeyListener(null);
        actTherapy.setFocusable(false);
        actTherapy.setOnClickListener(null);

        actStatus.setKeyListener(null);
        actStatus.setFocusable(false);
        actStatus.setOnClickListener(null);
    }

    private void loadExistingSessionData() {
        btnSave.setEnabled(false);

        sessionRepository.getSessionById(
                sessionId,
                new Callback<AdminSessionResponse>() {
                    @Override
                    public void onResponse(
                            Call<AdminSessionResponse> call,
                            Response<AdminSessionResponse> response) {

                        if (isFinishing() || isDestroyed()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            currentSession = response.body();
                            populateFields();
                        } else {
                            showToast("Session load nahi hua. HTTP "
                                    + response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminSessionResponse> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) return;

                        showToast("Server se session load nahi hua.");
                        android.util.Log.e(
                                "AdminEditSession",
                                "Load failed for session " + sessionId,
                                t
                        );
                    }
                }
        );
    }

    private void populateFields() {
        actBranch.setText(safeText(currentSession.getBranchName()), false);
        actChild.setText(safeText(currentSession.getChildName()), false);
        actTherapist.setText(safeText(currentSession.getTherapistName()), false);
        actTherapy.setText(safeText(currentSession.getTherapyType()), false);
        actStatus.setText(formatStatus(currentSession.getStatus()), false);

        sessionDateForApi = safeValue(currentSession.getSessionDate());
        etDate.setText(formatDateForDisplay(sessionDateForApi));

        startTimeForApi = normalizeTime(currentSession.getStartTime());
        endTimeForApi = normalizeTime(currentSession.getEndTime());

        etTime.setText(formatTimeForDisplay(startTimeForApi));
        etEndTime.setText(formatTimeForDisplay(endTimeForApi));

        etRemarks.setText(currentSession.getRemarks());

        boolean editable = "SCHEDULED".equalsIgnoreCase(
                currentSession.getStatus()
        );

        btnSave.setEnabled(editable);

        if (!editable) {
            showToast("Sirf Scheduled session edit ho sakta hai.");
        }
    }

    private void setupPickers() {
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            if (!TextUtils.isEmpty(sessionDateForApi)) {
                try {
                    Date parsed = new SimpleDateFormat(
                            "yyyy-MM-dd", Locale.US
                    ).parse(sessionDateForApi);

                    if (parsed != null) {
                        calendar.setTime(parsed);
                    }
                } catch (ParseException ignored) {
                }
            }

            new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth);

                        sessionDateForApi = new SimpleDateFormat(
                                "yyyy-MM-dd", Locale.US
                        ).format(selected.getTime());

                        etDate.setText(new SimpleDateFormat(
                                "dd MMM yyyy", Locale.getDefault()
                        ).format(selected.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        etTime.setOnClickListener(v -> showTimePicker(true));
        etEndTime.setOnClickListener(v -> showTimePicker(false));
    }

    private void showTimePicker(boolean isStartTime) {
        String existingTime = isStartTime
                ? startTimeForApi
                : endTimeForApi;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);

        if (!TextUtils.isEmpty(existingTime) && existingTime.length() >= 5) {
            try {
                hour = Integer.parseInt(existingTime.substring(0, 2));
                minute = Integer.parseInt(existingTime.substring(3, 5));
            } catch (NumberFormatException ignored) {
            }
        }

        new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    String apiTime = String.format(
                            Locale.US, "%02d:%02d:00",
                            selectedHour, selectedMinute
                    );

                    String displayTime = String.format(
                            Locale.US, "%02d:%02d %s",
                            selectedHour % 12 == 0 ? 12 : selectedHour % 12,
                            selectedMinute,
                            selectedHour >= 12 ? "PM" : "AM"
                    );

                    if (isStartTime) {
                        startTimeForApi = apiTime;
                        etTime.setText(displayTime);
                    } else {
                        endTimeForApi = apiTime;
                        etEndTime.setText(displayTime);
                    }
                },
                hour,
                minute,
                false
        ).show();
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        if (currentSession == null || sessionId == null) {
            showToast("Session details abhi load nahi hui.");
            return;
        }

        if (!"SCHEDULED".equalsIgnoreCase(currentSession.getStatus())) {
            showToast("Sirf Scheduled session edit ho sakta hai.");
            return;
        }

        if (TextUtils.isEmpty(sessionDateForApi)) {
            etDate.setError("Date select karo");
            return;
        }

        if (TextUtils.isEmpty(startTimeForApi)) {
            etTime.setError("Start Time select karo");
            return;
        }

        if (TextUtils.isEmpty(endTimeForApi)) {
            etEndTime.setError("End Time select karo");
            return;
        }

        if (startTimeForApi.compareTo(endTimeForApi) >= 0) {
            etEndTime.setError("End Time, Start Time ke baad hona chahiye");
            showToast("End Time, Start Time ke baad hona chahiye.");
            return;
        }

        String remarks = etRemarks.getText() == null
                ? ""
                : etRemarks.getText().toString().trim();

        AdminSessionUpdateRequest request =
                new AdminSessionUpdateRequest(
                        sessionDateForApi,
                        startTimeForApi,
                        endTimeForApi,
                        remarks
                );

        requestInProgress = true;
        btnSave.setEnabled(false);

        sessionRepository.updateSession(
                sessionId,
                request,
                new Callback<AdminSessionResponse>() {
                    @Override
                    public void onResponse(
                            Call<AdminSessionResponse> call,
                            Response<AdminSessionResponse> response) {

                        requestInProgress = false;

                        if (isFinishing() || isDestroyed()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            showToast("Session successfully updated.");

                            setResult(RESULT_OK);
                            finish();
                        } else {
                            btnSave.setEnabled(true);
                            showToast("Update failed. HTTP "
                                    + response.code()
                                    + ". Session status ya time check karo.");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminSessionResponse> call,
                            Throwable t) {

                        requestInProgress = false;

                        if (isFinishing() || isDestroyed()) return;

                        btnSave.setEnabled(true);
                        showToast("Session update ke liye server connect nahi hua.");

                        android.util.Log.e(
                                "AdminEditSession",
                                "Update failed for session " + sessionId,
                                t
                        );
                    }
                }
        );
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeText(String value) {
        return TextUtils.isEmpty(value) ? "Not provided" : value;
    }

    private String normalizeTime(String time) {
        if (TextUtils.isEmpty(time)) return "";

        // Backend LocalTime may return HH:mm:ss or HH:mm.
        if (time.length() >= 8) return time.substring(0, 8);
        if (time.length() == 5) return time + ":00";

        return time;
    }

    private String formatTimeForDisplay(String time) {
        if (TextUtils.isEmpty(time) || time.length() < 5) {
            return "";
        }

        try {
            int hour = Integer.parseInt(time.substring(0, 2));
            int minute = Integer.parseInt(time.substring(3, 5));

            return String.format(
                    Locale.US,
                    "%02d:%02d %s",
                    hour % 12 == 0 ? 12 : hour % 12,
                    minute,
                    hour >= 12 ? "PM" : "AM"
            );
        } catch (NumberFormatException e) {
            return time;
        }
    }

    private String formatDateForDisplay(String date) {
        if (TextUtils.isEmpty(date)) return "";

        try {
            Date parsed = new SimpleDateFormat(
                    "yyyy-MM-dd", Locale.US
            ).parse(date);

            if (parsed != null) {
                return new SimpleDateFormat(
                        "dd MMM yyyy", Locale.getDefault()
                ).format(parsed);
            }
        } catch (ParseException ignored) {
        }

        return date;
    }

    private String formatStatus(String status) {
        if (status == null) return "Unknown";

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
                AdminEditSessionActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
