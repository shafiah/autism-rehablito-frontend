package com.autismcentre.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.view.View;

import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminBranchResponse;

import com.autismcentre.model.AdminCreateNotificationRequest;
import com.autismcentre.model.AdminScheduleNotificationRequest;

import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class AdminCreateNotificationActivity
        extends BaseAdminActivity {

    private MaterialAutoCompleteTextView
            actAdminCreateNotificationType;

    private MaterialAutoCompleteTextView
            actAdminCreateNotificationRecipient;

    private MaterialAutoCompleteTextView
            actAdminCreateNotificationBranch;

    private MaterialAutoCompleteTextView
            actAdminCreateNotificationSendMode;

    private TextInputEditText
            etAdminCreateNotificationTitle;

    private TextInputEditText
            etAdminCreateNotificationMessage;

    private MaterialButton
            btnAdminCreateNotification;

    private MaterialButton
            btnAdminCreateNotificationCancel;

    private String selectedNotificationType =
            "General";

    private String selectedNotificationRecipient =
            "Parent";

    private String selectedNotificationBranch =
            "All Branches";

    private String selectedNotificationSendMode =
            "Send Now";

    private ApiService apiService;

    private MaterialButton btnAdminSelectScheduleDate;
    private MaterialButton btnAdminSelectScheduleTime;

    private Long selectedBranchId = null;

    private final List<AdminBranchResponse> adminBranches =
            new ArrayList<>();

    private Calendar selectedScheduleCalendar;

    private boolean isScheduleDateSelected = false;
    private boolean isScheduleTimeSelected = false;
    private View layoutAdminScheduleDateTime;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_create_notification
        );

        initializeAdminCreateNotificationViews();

        setupAdminCreateNotificationType();

        setupAdminCreateNotificationRecipient();

        setupAdminCreateNotificationBranch();
        ApiClient.initialize(this);
        apiService = ApiClient.getApiService();

        loadAdminBranches();

        setupAdminScheduleDateTime();

        setupAdminCreateNotificationSendMode();

        setupAdminCreateNotificationButtons();
    }

    private void initializeAdminCreateNotificationViews() {

        actAdminCreateNotificationType =
                findViewById(
                        R.id.actAdminCreateNotificationType
                );

        actAdminCreateNotificationRecipient =
                findViewById(
                        R.id.actAdminCreateNotificationRecipient
                );

        actAdminCreateNotificationBranch =
                findViewById(
                        R.id.actAdminCreateNotificationBranch
                );

        actAdminCreateNotificationSendMode =
                findViewById(
                        R.id.actAdminCreateNotificationSendMode
                );

        etAdminCreateNotificationTitle =
                findViewById(
                        R.id.etAdminCreateNotificationTitle
                );

        etAdminCreateNotificationMessage =
                findViewById(
                        R.id.etAdminCreateNotificationMessage
                );

        btnAdminCreateNotification =
                findViewById(
                        R.id.btnAdminCreateNotification
                );

        btnAdminCreateNotificationCancel =
                findViewById(
                        R.id.btnAdminCreateNotificationCancel
                );

        btnAdminSelectScheduleDate =
                findViewById(
                        R.id.btnAdminSelectScheduleDate
                );

        btnAdminSelectScheduleTime =
                findViewById(
                        R.id.btnAdminSelectScheduleTime
                );

        findViewById(R.id.layoutAdminScheduleDateTime);

        layoutAdminScheduleDateTime =
                findViewById(R.id.layoutAdminScheduleDateTime);
    }

    private void setupAdminCreateNotificationType() {

        String[] notificationTypes = {
                "General",
                "Session",
                "Payment",
                "Homework",
                "Report",
                "Announcement"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_dropdown_item_1line,
                        notificationTypes
                );

        actAdminCreateNotificationType.setAdapter(
                adapter
        );

        actAdminCreateNotificationType.setText(
                "General",
                false
        );

        actAdminCreateNotificationType.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedNotificationType =
                            notificationTypes[position];
                }
        );
    }

    private void setupAdminCreateNotificationRecipient() {

        String[] recipients = {
                "Parent",
                "Therapist",
                "Both"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_dropdown_item_1line,
                        recipients
                );

        actAdminCreateNotificationRecipient.setAdapter(
                adapter
        );

        actAdminCreateNotificationRecipient.setText(
                "Parent",
                false
        );

        actAdminCreateNotificationRecipient
                .setOnItemClickListener(
                        (parent, view, position, id) -> {

                            selectedNotificationRecipient =
                                    recipients[position];
                        }
                );
    }

    private void setupAdminCreateNotificationBranch() {

        actAdminCreateNotificationBranch.setText(
                "All Branches",
                false
        );

        actAdminCreateNotificationBranch.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position == 0) {

                        selectedNotificationBranch = "All Branches";
                        selectedBranchId = null;

                    } else {

                        AdminBranchResponse branch =
                                adminBranches.get(position - 1);

                        selectedNotificationBranch =
                                branch.getBranchName();

                        selectedBranchId = branch.getId();
                    }
                }
        );
    }

    private void loadAdminBranches() {

        apiService.getAdminBranches().enqueue(
                new Callback<List<AdminBranchResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminBranchResponse>> call,
                            Response<List<AdminBranchResponse>> response
                    ) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Toast.makeText(
                                    AdminCreateNotificationActivity.this,
                                    "Unable to load branches.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        adminBranches.clear();

                        List<String> branchNames =
                                new ArrayList<>();

                        branchNames.add("All Branches");

                        for (AdminBranchResponse branch
                                : response.body()) {

                            if (branch.getId() == null
                                    || branch.getBranchName() == null
                                    || !Boolean.TRUE.equals(branch.getActive())) {
                                continue;
                            }

                            adminBranches.add(branch);

                            branchNames.add(
                                    branch.getBranchName()
                            );
                        }

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(
                                        AdminCreateNotificationActivity.this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        branchNames
                                );

                        actAdminCreateNotificationBranch.setAdapter(
                                adapter
                        );

                        actAdminCreateNotificationBranch.setText(
                                "All Branches",
                                false
                        );

                        selectedBranchId = null;
                        selectedNotificationBranch = "All Branches";
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                AdminCreateNotificationActivity.this,
                                "Branch loading failed: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
    private void setupAdminCreateNotificationSendMode() {

        String[] sendModes = {
                "Send Now",
                "Scheduled"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        sendModes
                );

        actAdminCreateNotificationSendMode.setAdapter(adapter);

        actAdminCreateNotificationSendMode.setText(
                "Send Now",
                false
        );

       // btnAdminSelectScheduleDate.setVisibility(View.GONE);
       // btnAdminSelectScheduleTime.setVisibility(View.GONE);
        layoutAdminScheduleDateTime.setVisibility(View.GONE);

        actAdminCreateNotificationSendMode.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedNotificationSendMode =
                            sendModes[position];

                    boolean scheduled =
                            "Scheduled".equals(selectedNotificationSendMode);

                    layoutAdminScheduleDateTime.setVisibility(
                            scheduled ? View.VISIBLE : View.GONE
                    );

                    if (!scheduled) {

                        isScheduleDateSelected = false;
                        isScheduleTimeSelected = false;

                        selectedScheduleCalendar = null;

                        btnAdminSelectScheduleDate.setText(
                                "Select Schedule Date"
                        );

                        btnAdminSelectScheduleTime.setText(
                                "Select Schedule Time"
                        );
                    }
                }
        );
    }

    private void setupAdminScheduleDateTime() {

        btnAdminSelectScheduleDate.setOnClickListener(
                v -> showScheduleDatePicker()
        );

        btnAdminSelectScheduleTime.setOnClickListener(
                v -> {

                    if (!isScheduleDateSelected) {

                        Toast.makeText(
                                this,
                                "Please select schedule date first.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    showScheduleTimePicker();
                }
        );
    }

    private void showScheduleDatePicker() {

        Calendar now = Calendar.getInstance();

        Calendar initial = selectedScheduleCalendar != null
                ? (Calendar) selectedScheduleCalendar.clone()
                : (Calendar) now.clone();

        DatePickerDialog datePicker =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            Calendar selected =
                                    Calendar.getInstance();

                            selected.set(
                                    year,
                                    month,
                                    dayOfMonth
                            );

                            selected.set(
                                    Calendar.HOUR_OF_DAY,
                                    0
                            );

                            selected.set(
                                    Calendar.MINUTE,
                                    0
                            );

                            selected.set(
                                    Calendar.SECOND,
                                    0
                            );

                            Calendar today =
                                    Calendar.getInstance();

                            today.set(
                                    Calendar.HOUR_OF_DAY,
                                    0
                            );

                            today.set(
                                    Calendar.MINUTE,
                                    0
                            );

                            today.set(
                                    Calendar.SECOND,
                                    0
                            );

                            if (selected.before(today)) {

                                Toast.makeText(
                                        this,
                                        "Past date is not allowed.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            selectedScheduleCalendar = selected;

                            isScheduleDateSelected = true;
                            isScheduleTimeSelected = false;

                            btnAdminSelectScheduleDate.setText(
                                    new SimpleDateFormat(
                                            "dd MMM yyyy",
                                            Locale.ENGLISH
                                    ).format(selected.getTime())
                            );

                            btnAdminSelectScheduleTime.setText(
                                    "Select Schedule Time"
                            );
                        },
                        initial.get(Calendar.YEAR),
                        initial.get(Calendar.MONTH),
                        initial.get(Calendar.DAY_OF_MONTH)
                );

        datePicker.getDatePicker().setMinDate(
                System.currentTimeMillis()
        );

        datePicker.show();
    }

    private void showScheduleTimePicker() {

        Calendar initial = selectedScheduleCalendar;

        TimePickerDialog timePicker =
                new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {

                            Calendar selected =
                                    (Calendar) selectedScheduleCalendar.clone();

                            selected.set(
                                    Calendar.HOUR_OF_DAY,
                                    hourOfDay
                            );

                            selected.set(
                                    Calendar.MINUTE,
                                    minute
                            );

                            selected.set(
                                    Calendar.SECOND,
                                    0
                            );

                            if (selected.before(Calendar.getInstance())) {

                                Toast.makeText(
                                        this,
                                        "Please select a future time.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            selectedScheduleCalendar = selected;

                            isScheduleTimeSelected = true;

                            btnAdminSelectScheduleTime.setText(
                                    new SimpleDateFormat(
                                            "hh:mm a",
                                            Locale.ENGLISH
                                    ).format(selected.getTime())
                            );
                        },
                        initial.get(Calendar.HOUR_OF_DAY),
                        initial.get(Calendar.MINUTE),
                        false
                );

        timePicker.show();
    }

    private String getScheduledDateTime() {

        if (!isScheduleDateSelected
                || !isScheduleTimeSelected
                || selectedScheduleCalendar == null) {

            return null;
        }

        return new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.ENGLISH
        ).format(selectedScheduleCalendar.getTime());
    }
    private void setupAdminCreateNotificationButtons() {

        btnAdminCreateNotification.setOnClickListener(
                v -> createAdminNotification()
        );

        btnAdminCreateNotificationCancel.setOnClickListener(
                v -> finish()
        );
    }


    private void createAdminNotification() {

        String title =
                etAdminCreateNotificationTitle
                        .getText()
                        .toString()
                        .trim();

        String message =
                etAdminCreateNotificationMessage
                        .getText()
                        .toString()
                        .trim();

        if (title.isEmpty()) {

            etAdminCreateNotificationTitle.setError(
                    "Enter notification title"
            );

            etAdminCreateNotificationTitle.requestFocus();

            return;
        }

        if (message.isEmpty()) {

            etAdminCreateNotificationMessage.setError(
                    "Enter notification message"
            );

            etAdminCreateNotificationMessage.requestFocus();

            return;
        }

        String notificationType =
                getBackendNotificationType(
                        selectedNotificationType
                );

        String recipient =
                getBackendRecipient(
                        selectedNotificationRecipient
                );

        if ("Scheduled".equals(selectedNotificationSendMode)) {

            String scheduledAt = getScheduledDateTime();

            if (scheduledAt == null) {

                Toast.makeText(
                        this,
                        "Please select schedule date and time.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            AdminScheduleNotificationRequest request =
                    new AdminScheduleNotificationRequest(
                            title,
                            message,
                            notificationType,
                            recipient,
                            selectedBranchId,
                            scheduledAt
                    );

            submitScheduledNotification(request);

        } else {

            AdminCreateNotificationRequest request =
                    new AdminCreateNotificationRequest(
                            title,
                            message,
                            notificationType,
                            recipient,
                            selectedBranchId
                    );

            submitImmediateNotification(request);
        }
    }

    private String getBackendNotificationType(String type) {

        if (type == null) {
            return "SYSTEM";
        }

        switch (type) {

            case "Session":
                return "SESSION";

            case "Payment":
                return "PAYMENT";

            case "Homework":
                return "HOMEWORK";

            case "Report":
                return "REPORT";

            case "General":
            case "Announcement":
            default:
                return "SYSTEM";
        }
    }

    private String getBackendRecipient(String recipient) {

        if (recipient == null) {
            return "PARENT";
        }

        switch (recipient) {

            case "Therapist":
                return "THERAPIST";

            case "Both":
                return "BOTH";

            case "Parent":
            default:
                return "PARENT";
        }
    }

    private void submitImmediateNotification(
            AdminCreateNotificationRequest request
    ) {

        btnAdminCreateNotification.setEnabled(false);

        apiService.createAdminNotification(request).enqueue(
                new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response
                    ) {

                        btnAdminCreateNotification.setEnabled(true);

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    AdminCreateNotificationActivity.this,
                                    "Notification created successfully.",
                                    Toast.LENGTH_LONG
                            ).show();

                            setResult(RESULT_OK);
                            finish();

                        } else {

                            Toast.makeText(
                                    AdminCreateNotificationActivity.this,
                                    "Notification failed. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable t
                    ) {

                        btnAdminCreateNotification.setEnabled(true);

                        Toast.makeText(
                                AdminCreateNotificationActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void submitScheduledNotification(
            AdminScheduleNotificationRequest request
    ) {

        btnAdminCreateNotification.setEnabled(false);

        apiService.scheduleAdminNotification(request).enqueue(
                new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response
                    ) {

                        btnAdminCreateNotification.setEnabled(true);

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    AdminCreateNotificationActivity.this,
                                    "Notification scheduled successfully.",
                                    Toast.LENGTH_LONG
                            ).show();

                            setResult(RESULT_OK);
                            finish();

                        } else {

                            Toast.makeText(
                                    AdminCreateNotificationActivity.this,
                                    "Scheduling failed. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable t
                    ) {

                        btnAdminCreateNotification.setEnabled(true);

                        Toast.makeText(
                                AdminCreateNotificationActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

}