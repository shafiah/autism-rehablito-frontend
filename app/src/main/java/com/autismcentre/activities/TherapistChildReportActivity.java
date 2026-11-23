
package com.autismcentre.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.TherapistNoteRequest;
import com.autismcentre.model.TherapistNoteResponse;
import com.autismcentre.model.TherapistProgressRequest;
import com.autismcentre.model.TherapistProgressResponse;
import com.autismcentre.model.TherapistReportCreateRequest;
import com.autismcentre.model.TherapistReportResponse;
import com.autismcentre.model.TherapistSessionResponse;
import com.google.android.material.button.MaterialButton;

import android.widget.SeekBar;
import android.widget.TextView;

import android.widget.ProgressBar;

import com.autismcentre.model.TherapistChildResponse;

import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistChildReportActivity extends BaseTherapistActivity {

    private MaterialButton btnAddNote;
    private MaterialButton btnUpdateProgress;
    private MaterialButton btnCreateReport;
    private MaterialButton btnSubmitReport;

    private Long childId;
    private Long currentDraftReportId;
    private String childName;
    private ApiService apiService;

    private TextView tvReportChildName;
    private TextView tvReportAge;
    private TextView tvReportTherapy;
    private TextView tvReportTherapist;
    private TextView tvReportProgress;
    private TextView tvCompletedSessions;
    private TextView tvPendingSessions;
    private TextView tvLastSession;
    private TextView tvTherapistNotes;
    private TextView tvNextGoal;
    private ProgressBar progressReport;
    private LinearLayout therapistNotesContainer;
    private TextView tvSavedNotesMessage;

    private final List<TherapistSessionResponse> eligibleSessions =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageTitle("Rehablito Autism Center");
        showPageContent(R.layout.activity_therapist_child_report);

        btnAddNote = findViewById(R.id.btnAddNote);
        btnUpdateProgress = findViewById(R.id.btnUpdateProgress);
        btnCreateReport = findViewById(R.id.btnCreateReport);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);

        tvReportChildName = findViewById(R.id.tvReportChildName);
        tvReportAge = findViewById(R.id.tvReportAge);
        tvReportTherapy = findViewById(R.id.tvReportTherapy);
        tvReportTherapist = findViewById(R.id.tvReportTherapist);
        tvReportProgress = findViewById(R.id.tvReportProgress);
        progressReport = findViewById(R.id.progressReport);
        tvCompletedSessions = findViewById(R.id.tvCompletedSessions);
        tvPendingSessions = findViewById(R.id.tvPendingSessions);
        tvLastSession = findViewById(R.id.tvLastSession);
        tvTherapistNotes = findViewById(R.id.tvTherapistNotes);
        tvNextGoal = findViewById(R.id.tvNextGoal);
        therapistNotesContainer = findViewById(R.id.therapistNotesContainer);
        tvSavedNotesMessage = findViewById(R.id.tvSavedNotesMessage);

        childName = getIntent().getStringExtra("child_name");
        if (childName == null || childName.trim().isEmpty()) {
            childName = "Child";
        }

        childId = getIntent().hasExtra("child_id")
                ? getIntent().getLongExtra("child_id", -1L)
                : -1L;

        apiService = ApiClient.getApiService();
        if (childId != null && childId > 0) {
            loadChildReportOverview();
        } else {
            Toast.makeText(
                    this,
                    "Child ID missing. Open this screen from Assigned Children.",
                    Toast.LENGTH_LONG
            ).show();
        }

        btnAddNote.setOnClickListener(v -> showAddNoteDialog());
        btnUpdateProgress.setOnClickListener(v -> loadEligibleSessions());
        btnCreateReport.setOnClickListener(v -> showCreateReportDialog());
        btnSubmitReport.setOnClickListener(v -> submitCurrentDraft());

        boolean openProgressDialog = getIntent().getBooleanExtra(
                "open_progress_dialog", false);

        if (openProgressDialog) {
            btnUpdateProgress.post(this::loadEligibleSessions);
        }
    }

    // =====================================================
// LOAD DYNAMIC CHILD REPORT OVERVIEW
// =====================================================

    private void loadChildReportOverview() {
        loadReportChildDetails();
        loadReportSessions();
        loadReportProgress();
        loadSavedTherapistNotes();
    }


    private void loadSavedTherapistNotes() {

        if (childId == null || childId <= 0) {
            return;
        }

        tvSavedNotesMessage.setText("Loading saved notes...");
        tvSavedNotesMessage.setVisibility(View.VISIBLE);
        therapistNotesContainer.removeAllViews();

        apiService.getTherapistChildNotes(childId).enqueue(
                new Callback<List<TherapistNoteResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TherapistNoteResponse>> call,
                            Response<List<TherapistNoteResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        therapistNotesContainer.removeAllViews();

                        if (!response.isSuccessful() || response.body() == null) {
                            tvSavedNotesMessage.setText(
                                    "Unable to load saved notes. HTTP "
                                            + response.code());
                            tvSavedNotesMessage.setVisibility(View.VISIBLE);
                            return;
                        }

                        List<TherapistNoteResponse> notes = response.body();

                        if (notes.isEmpty()) {
                            tvSavedNotesMessage.setText(
                                    "No therapist notes saved yet.");
                            tvSavedNotesMessage.setVisibility(View.VISIBLE);
                            return;
                        }

                        tvSavedNotesMessage.setVisibility(View.GONE);

                        for (TherapistNoteResponse note : notes) {
                            therapistNotesContainer.addView(
                                    createTherapistNoteCard(note));
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistNoteResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed() || call.isCanceled()) {
                            return;
                        }

                        tvSavedNotesMessage.setText(
                                "Network error while loading saved notes.");
                        tvSavedNotesMessage.setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    private View createTherapistNoteCard(TherapistNoteResponse note) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackgroundColor(Color.parseColor("#F8F7FC"));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        cardParams.bottomMargin = dp(10);
        card.setLayoutParams(cardParams);

        TextView tvSession = new TextView(this);
        tvSession.setText(
                "Session #" + (note.getSessionId() == null
                        ? "N/A" : note.getSessionId()));
        tvSession.setTextSize(13);
        tvSession.setTextColor(Color.parseColor("#6750A4"));
        tvSession.setTypeface(null, Typeface.BOLD);
        card.addView(tvSession);

        TextView tvNote = new TextView(this);
        tvNote.setText(
                note.getNote() == null ? "" : note.getNote());
        tvNote.setTextSize(14);
        tvNote.setTextColor(Color.parseColor("#29243A"));
        tvNote.setPadding(0, dp(8), 0, dp(8));
        card.addView(tvNote);

        TextView tvDate = new TextView(this);
        tvDate.setText("Created: " + formatDateTime(note.getCreatedAt()));
        tvDate.setTextSize(12);
        tvDate.setTextColor(Color.parseColor("#6F6A7A"));
        card.addView(tvDate);

        return card;
    }

    private int dp(int value) {
        return (int) (value
                * getResources().getDisplayMetrics().density
                + 0.5f);
    }

    private String formatDateTime(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "Not available";
        }

        // The backend may return an ISO date-time such as
        // 2026-10-17T10:30:00.
        String formatted = value.replace('T', ' ');

        if (formatted.length() >= 16) {
            return formatted.substring(0, 16);
        }

        return formatted;
    }


    private void loadReportChildDetails() {

        apiService.getMyTherapistChildById(childId).enqueue(
                new Callback<TherapistChildResponse>() {

                    @Override
                    public void onResponse(
                            Call<TherapistChildResponse> call,
                            Response<TherapistChildResponse> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {

                            TherapistChildResponse child = response.body();

                            String name = child.getChildName();

                            if (name != null && !name.trim().isEmpty()) {
                                childName = name;
                                tvReportChildName.setText(name);
                            } else {
                                tvReportChildName.setText("Child");
                            }

                            String dob = child.getDateOfBirth();
                            tvReportAge.setText("Age: " + calculateAge(dob));

                        } else {
                            tvReportAge.setText("Age: Not available");
                            Toast.makeText(
                                    TherapistChildReportActivity.this,
                                    "Unable to load child details. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TherapistChildResponse> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed() || call.isCanceled()) {
                            return;
                        }

                        tvReportAge.setText("Age: Not available");
                    }
                }
        );
    }

    private void loadReportSessions() {

        apiService.getMyTherapistSessions().enqueue(
                new Callback<List<TherapistSessionResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TherapistSessionResponse>> call,
                            Response<List<TherapistSessionResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            tvReportTherapy.setText("Therapy: Not available");
                            tvReportTherapist.setText("Therapist: Not available");
                            tvCompletedSessions.setText("Completed Sessions: --");
                            tvPendingSessions.setText("Pending Sessions: --");
                            tvLastSession.setText("Last Session: --");
                            return;
                        }

                        List<TherapistSessionResponse> childSessions =
                                new ArrayList<>();

                        for (TherapistSessionResponse session : response.body()) {

                            if (session == null
                                    || session.getChildId() == null
                                    || !childId.equals(session.getChildId())) {
                                continue;
                            }

                            childSessions.add(session);
                        }

                        int completed = 0;
                        int pending = 0;

                        String therapyType = null;
                        String therapistName = null;
                        String lastSessionDate = null;

                        for (TherapistSessionResponse session : childSessions) {

                            String status = session.getStatus();

                            if ("COMPLETED".equalsIgnoreCase(status)) {
                                completed++;
                            } else if ("SCHEDULED".equalsIgnoreCase(status)
                                    || "IN_PROGRESS".equalsIgnoreCase(status)) {
                                pending++;
                            }

                            if (therapyType == null
                                    && session.getTherapyType() != null
                                    && !session.getTherapyType().trim().isEmpty()) {
                                therapyType = session.getTherapyType();
                            }

                            if (therapistName == null
                                    && session.getTherapistName() != null
                                    && !session.getTherapistName().trim().isEmpty()) {
                                therapistName = session.getTherapistName();
                            }

                            String date = session.getSessionDate();

                            if (date != null && !date.trim().isEmpty()) {
                                if (lastSessionDate == null
                                        || date.compareTo(lastSessionDate) > 0) {
                                    lastSessionDate = date;
                                }
                            }
                        }

                        tvReportTherapy.setText(
                                "Therapy: " + (therapyType == null
                                        ? "Not available" : therapyType)
                        );

                        tvReportTherapist.setText(
                                "Therapist: " + (therapistName == null
                                        ? "Not available" : therapistName)
                        );

                        tvCompletedSessions.setText(
                                "Completed Sessions: " + completed
                        );

                        tvPendingSessions.setText(
                                "Pending Sessions: " + pending
                        );

                        tvLastSession.setText(
                                "Last Session: " + formatDate(lastSessionDate)
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistSessionResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed() || call.isCanceled()) {
                            return;
                        }

                        tvReportTherapy.setText("Therapy: Not available");
                        tvReportTherapist.setText("Therapist: Not available");
                        tvCompletedSessions.setText("Completed Sessions: --");
                        tvPendingSessions.setText("Pending Sessions: --");
                        tvLastSession.setText("Last Session: --");
                    }
                }
        );
    }

    private void loadReportProgress() {

        apiService.getTherapistChildProgress(childId).enqueue(
                new Callback<List<TherapistProgressResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TherapistProgressResponse>> call,
                            Response<List<TherapistProgressResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null
                                || response.body().isEmpty()) {

                            tvReportProgress.setText("--");
                            progressReport.setProgress(0);
                            tvTherapistNotes.setText("No notes available yet.");
                            tvNextGoal.setText("No goal recorded yet.");
                            return;
                        }

                        List<TherapistProgressResponse> records =
                                new ArrayList<>(response.body());

                        // Prefer the most recently updated record.
                        Collections.sort(records, new Comparator<TherapistProgressResponse>() {
                            @Override
                            public int compare(
                                    TherapistProgressResponse first,
                                    TherapistProgressResponse second) {

                                String firstDate = first.getUpdatedAt() != null
                                        ? first.getUpdatedAt() : first.getCreatedAt();

                                String secondDate = second.getUpdatedAt() != null
                                        ? second.getUpdatedAt() : second.getCreatedAt();

                                if (firstDate == null) return 1;
                                if (secondDate == null) return -1;

                                return secondDate.compareTo(firstDate);
                            }
                        });

                        TherapistProgressResponse latest = records.get(0);

                        String progressValue = latest.getProgress();
                        int percentage = parsePercentage(progressValue);

                        if (percentage >= 0) {
                            tvReportProgress.setText(percentage + "%");
                            progressReport.setProgress(percentage);
                        } else {
                            tvReportProgress.setText(
                                    progressValue == null || progressValue.trim().isEmpty()
                                            ? "--" : progressValue
                            );
                            progressReport.setProgress(0);
                        }

                        tvTherapistNotes.setVisibility(View.GONE);

                        String goals = latest.getGoals();

                        tvNextGoal.setText(
                                goals != null && !goals.trim().isEmpty()
                                        ? goals.trim()
                                        : "No goal recorded yet."
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistProgressResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed() || call.isCanceled()) {
                            return;
                        }

                        tvReportProgress.setText("--");
                        progressReport.setProgress(0);
                        tvTherapistNotes.setText("Unable to load notes.");
                        tvNextGoal.setText("Unable to load goals.");
                    }
                }
        );
    }

    private String calculateAge(String dob) {

        if (dob == null || dob.trim().isEmpty()) {
            return "Not available";
        }

        try {
            String datePart = dob.length() >= 10 ? dob.substring(0, 10) : dob;

            SimpleDateFormat format =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);

            Date birthDate = format.parse(datePart);

            if (birthDate == null || birthDate.after(new Date())) {
                return "Not available";
            }

            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);

            Calendar today = Calendar.getInstance();

            int years = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR)
                    < birth.get(Calendar.DAY_OF_YEAR)) {
                years--;
            }

            return years + (years == 1 ? " Year" : " Years");

        } catch (Exception e) {
            return "Not available";
        }
    }

    private String formatDate(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "--";
        }

        try {
            String datePart = value.length() >= 10
                    ? value.substring(0, 10) : value;

            SimpleDateFormat input =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            input.setLenient(false);

            Date date = input.parse(datePart);

            if (date == null) {
                return value;
            }

            SimpleDateFormat output =
                    new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

            return output.format(date);

        } catch (Exception e) {
            return value;
        }
    }

    private int parsePercentage(String value) {

        if (value == null || value.trim().isEmpty()) {
            return -1;
        }

        try {
            String digits = value.trim().replace("%", "").trim();
            int percentage = Integer.parseInt(digits);

            if (percentage < 0 || percentage > 100) {
                return -1;
            }

            return percentage;

        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // =====================================================
    // CREATE REPORT DIALOG
    // =====================================================

    private void showCreateReportDialog() {

        if (childId == null || childId <= 0) {
            Toast.makeText(this,
                    "Child ID missing. Open this page from Assigned Children.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_therapist_report_form, null);

        EditText etTitle =
                dialogView.findViewById(R.id.etTherapistReportTitle);

        EditText etContent =
                dialogView.findViewById(R.id.etTherapistReportContent);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Create Therapy Report")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save Draft", null)
                .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {

                            String title =
                                    etTitle.getText().toString().trim();

                            String content =
                                    etContent.getText().toString().trim();

                            if (title.isEmpty() || content.isEmpty()) {
                                Toast.makeText(this,
                                        "Report title and content are required",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (title.length() > 200
                                    || content.length() > 5000) {
                                Toast.makeText(this,
                                        "Title limit is 200 characters and content limit is 5000",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            createDraftReport(title, content, dialog);
                        })
        );

        dialog.show();
    }

    private void createDraftReport(
            String title,
            String content,
            AlertDialog dialog) {

        btnCreateReport.setEnabled(false);

        TherapistReportCreateRequest request =
                new TherapistReportCreateRequest(title, content);

        apiService.createTherapistReport(childId, request).enqueue(
                new Callback<TherapistReportResponse>() {

                    @Override
                    public void onResponse(
                            Call<TherapistReportResponse> call,
                            Response<TherapistReportResponse> response) {

                        btnCreateReport.setEnabled(true);

                        TherapistReportResponse body = response.body();

                        if (response.isSuccessful()
                                && body != null
                                && body.getReportId() != null) {

                            currentDraftReportId = body.getReportId();
                            dialog.dismiss();

                            Toast.makeText(
                                    TherapistChildReportActivity.this,
                                    "Draft saved. Report ID: "
                                            + currentDraftReportId
                                            + ". You can now submit it.",
                                    Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(
                                    TherapistChildReportActivity.this,
                                    "Could not create report. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TherapistReportResponse> call,
                            Throwable t) {

                        btnCreateReport.setEnabled(true);

                        Toast.makeText(
                                TherapistChildReportActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // =====================================================
    // SUBMIT CURRENT DRAFT
    // =====================================================

    private void submitCurrentDraft() {

        if (currentDraftReportId == null) {
            Toast.makeText(this,
                    "Create a report draft first. This screen does not load existing drafts.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Submit Report")
                .setMessage("Are you sure you want to submit this therapy report?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Submit", (dialog, which) ->
                        sendSubmitReportRequest())
                .show();
    }

    private void sendSubmitReportRequest() {

        btnSubmitReport.setEnabled(false);

        apiService.submitTherapistReport(currentDraftReportId).enqueue(
                new Callback<TherapistReportResponse>() {

                    @Override
                    public void onResponse(
                            Call<TherapistReportResponse> call,
                            Response<TherapistReportResponse> response) {

                        btnSubmitReport.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {

                            String status = response.body().getStatus();

                            Toast.makeText(
                                    TherapistChildReportActivity.this,
                                    "Report submitted successfully. Status: "
                                            + (status == null ? "SUBMITTED" : status),
                                    Toast.LENGTH_LONG).show();

                            currentDraftReportId = null;

                        } else {
                            Toast.makeText(
                                    TherapistChildReportActivity.this,
                                    "Could not submit report. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TherapistReportResponse> call,
                            Throwable t) {

                        btnSubmitReport.setEnabled(true);

                        Toast.makeText(
                                TherapistChildReportActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // =====================================================
    // ADD THERAPIST NOTE
    // =====================================================


    private void showAddNoteDialog() {

        if (childId == null || childId <= 0) {
            Toast.makeText(this,
                    "Child ID missing. Open this page from Assigned Children.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        btnAddNote.setEnabled(false);

        apiService.getMyTherapistSessions().enqueue(
                new Callback<List<TherapistSessionResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TherapistSessionResponse>> call,
                            Response<List<TherapistSessionResponse>> response) {

                        btnAddNote.setEnabled(true);

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(
                                    TherapistChildReportActivity.this,
                                    "Unable to load sessions. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        eligibleSessions.clear();

                        for (TherapistSessionResponse session : response.body()) {

                            if (session == null
                                    || session.getSessionId() == null
                                    || session.getChildId() == null
                                    || !childId.equals(session.getChildId())) {
                                continue;
                            }

                            String status = session.getStatus();

                            if ("IN_PROGRESS".equalsIgnoreCase(status)
                                    || "COMPLETED".equalsIgnoreCase(status)) {
                                eligibleSessions.add(session);
                            }
                        }

                        showNoteSessionSelectionDialog();
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistSessionResponse>> call,
                            Throwable t) {

                        btnAddNote.setEnabled(true);

                        if (isFinishing() || isDestroyed() || call.isCanceled()) {
                            return;
                        }

                        Toast.makeText(
                                TherapistChildReportActivity.this,
                                "Network error while loading sessions: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void showNoteSessionSelectionDialog() {

        if (eligibleSessions.isEmpty()) {
            Toast.makeText(this,
                    "No in-progress or completed sessions found for " + childName,
                    Toast.LENGTH_LONG).show();
            return;
        }

        String[] options = new String[eligibleSessions.size()];

        for (int i = 0; i < eligibleSessions.size(); i++) {
            TherapistSessionResponse session = eligibleSessions.get(i);

            options[i] = "Session #" + session.getSessionId()
                    + " • " + safe(session.getTherapyType())
                    + "\n" + safe(session.getSessionDate())
                    + " • " + safe(session.getStatus());
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Session for " + childName)
                .setItems(options, (dialog, which) ->
                        showNoteEntryDialog(eligibleSessions.get(which)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showNoteEntryDialog(TherapistSessionResponse session) {

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_therapist_add_note, null);

        EditText etNote = dialogView.findViewById(R.id.etTherapistNote);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Therapist Note")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save Note", null)
                .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {

                            String noteText =
                                    etNote.getText().toString().trim();

                            if (noteText.isEmpty()) {
                                etNote.setError("Please enter a note");
                                return;
                            }

                            if (noteText.length() > 2000) {
                                etNote.setError(
                                        "Note cannot exceed 2000 characters");
                                return;
                            }

                            saveTherapistNote(
                                    session.getSessionId(),
                                    noteText,
                                    dialog
                            );
                        })
        );

        dialog.show();
    }

    private void saveTherapistNote(
            Long sessionId,
            String noteText,
            AlertDialog dialog) {

        btnAddNote.setEnabled(false);

        TherapistNoteRequest request =
                new TherapistNoteRequest(noteText);

        apiService.createTherapistNote(sessionId, request).enqueue(
                new Callback<TherapistNoteResponse>() {

                    @Override
                    public void onResponse(
                            Call<TherapistNoteResponse> call,
                            Response<TherapistNoteResponse> response) {

                        btnAddNote.setEnabled(true);

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {

                            dialog.dismiss();

                            loadSavedTherapistNotes();

                            Toast.makeText(
                                    TherapistChildReportActivity.this,
                                    "Therapist note saved successfully",
                                    Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(
                                    TherapistChildReportActivity.this,
                                    "Could not save note. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TherapistNoteResponse> call,
                            Throwable t) {

                        btnAddNote.setEnabled(true);

                        if (isFinishing() || isDestroyed() || call.isCanceled()) {
                            return;
                        }

                        Toast.makeText(
                                TherapistChildReportActivity.this,
                                "Network error while saving note: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }


    // =====================================================
    // LOAD ELIGIBLE SESSIONS FOR PROGRESS
    // =====================================================

    private void loadEligibleSessions() {

        if (childId == null || childId <= 0) {
            Toast.makeText(this,
                    "Child ID missing. Open this page from Assigned Children.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        btnUpdateProgress.setEnabled(false);

        apiService.getMyTherapistSessions().enqueue(
                new Callback<List<TherapistSessionResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TherapistSessionResponse>> call,
                            Response<List<TherapistSessionResponse>> response) {

                        btnUpdateProgress.setEnabled(true);

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(
                                    TherapistChildReportActivity.this,
                                    "Unable to load sessions. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        eligibleSessions.clear();

                        for (TherapistSessionResponse session : response.body()) {

                            if (session == null
                                    || session.getChildId() == null
                                    || !childId.equals(session.getChildId())
                                    || session.getSessionId() == null) {
                                continue;
                            }

                            String status = session.getStatus();

                            if (status != null
                                    && ("IN_PROGRESS".equalsIgnoreCase(status)
                                    || "COMPLETED".equalsIgnoreCase(status))) {
                                eligibleSessions.add(session);
                            }
                        }

                        showSessionSelectionDialog();
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistSessionResponse>> call,
                            Throwable t) {

                        btnUpdateProgress.setEnabled(true);

                        Toast.makeText(
                                TherapistChildReportActivity.this,
                                "Network error while loading sessions: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showSessionSelectionDialog() {

        if (eligibleSessions.isEmpty()) {
            Toast.makeText(this,
                    "No IN_PROGRESS or COMPLETED sessions found for "
                            + childName,
                    Toast.LENGTH_LONG).show();
            return;
        }

        String[] options = new String[eligibleSessions.size()];

        for (int i = 0; i < eligibleSessions.size(); i++) {
            TherapistSessionResponse session = eligibleSessions.get(i);

            options[i] = "Session #" + session.getSessionId()
                    + " • " + safe(session.getTherapyType())
                    + "\n" + safe(session.getSessionDate())
                    + " • " + safe(session.getStatus());
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Session for " + childName)
                .setItems(options, (dialog, which) ->
                        showProgressForm(eligibleSessions.get(which)))
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void showProgressForm(TherapistSessionResponse session) {

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_therapist_progress_form, null);

        EditText etObservations =
                dialogView.findViewById(R.id.etProgressObservations);

        TextView tvProgressValue =
                dialogView.findViewById(R.id.tvProgressValue);

        SeekBar seekBarProgress =
                dialogView.findViewById(R.id.seekBarProgress);

        EditText etGoals =
                dialogView.findViewById(R.id.etProgressGoals);

        EditText etActivities =
                dialogView.findViewById(R.id.etProgressActivities);

        EditText etRemarks =
                dialogView.findViewById(R.id.etProgressRemarks);

        // Display the selected percentage.
        tvProgressValue.setText(seekBarProgress.getProgress() + "%");

        seekBarProgress.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser) {

                        tvProgressValue.setText(progress + "%");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // No additional action required.
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Selected percentage remains displayed.
                    }
                }
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Progress • Session #" + session.getSessionId())
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save Progress", null)
                .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {

                            String observations =
                                    etObservations.getText().toString().trim();

                            String progress =
                                    seekBarProgress.getProgress() + "%";

                            String goals =
                                    etGoals.getText().toString().trim();

                            String activities =
                                    etActivities.getText().toString().trim();

                            String remarks =
                                    etRemarks.getText().toString().trim();

                            if (observations.isEmpty()
                                    || goals.isEmpty()
                                    || activities.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "Fill observations, goals and activities",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            TherapistProgressRequest request =
                                    new TherapistProgressRequest(
                                            observations,
                                            progress,
                                            goals,
                                            activities,
                                            remarks
                                    );

                            submitProgress(
                                    session.getSessionId(),
                                    request,
                                    dialog
                            );
                        })
        );

        dialog.show();
    }

    private void submitProgress(
            Long sessionId,
            TherapistProgressRequest request,
            AlertDialog dialog) {

        apiService.createTherapistProgress(sessionId, request).enqueue(
                new Callback<TherapistProgressResponse>() {

                    @Override
                    public void onResponse(
                            Call<TherapistProgressResponse> call,
                            Response<TherapistProgressResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            dialog.dismiss();

                            Toast.makeText(
                                    TherapistChildReportActivity.this,
                                    "Therapy progress saved successfully",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(
                                    TherapistChildReportActivity.this,
                                    "Could not save progress. HTTP "
                                            + response.code()
                                            + ". Check whether this session already has a progress record.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TherapistProgressResponse> call,
                            Throwable t) {

                        Toast.makeText(
                                TherapistChildReportActivity.this,
                                "Network error while saving progress: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "N/A" : value;
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.therapist_nav_reports;
    }
}
