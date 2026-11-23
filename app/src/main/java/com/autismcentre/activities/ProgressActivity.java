
package com.autismcentre.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.model.ProgressRecordResponse;
import com.autismcentre.repository.ParentProgressRepository;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressActivity extends BaseParentActivity {

    private static final String TAG = "ProgressActivity";

    private ParentProgressRepository progressRepository;

    private TextView tvProgressChildName;
    private TextView tvOverallProgress;
    private TextView tvTherapistObservation;

    private ProgressBar progressOverallBar;

    private LinearLayout therapyProgressContainer;
    private LinearLayout recentProgressContainer;
    private LinearLayout currentGoalsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_progress);
        setPageTitle("Autism Centre");

        // Initialize API before creating the repository.
        ApiClient.initialize(getApplicationContext());
        progressRepository = new ParentProgressRepository();

        initializeViews();
        loadChildProgress();
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.nav_progress;
    }

    private void initializeViews() {
        tvProgressChildName = findViewById(R.id.tvProgressChildName);
        tvOverallProgress = findViewById(R.id.tvOverallProgress);
        tvTherapistObservation = findViewById(R.id.tvTherapistObservation);

        progressOverallBar = findViewById(R.id.progressOverallBar);

        therapyProgressContainer =
                findViewById(R.id.therapyProgressContainer);

        recentProgressContainer =
                findViewById(R.id.recentProgressContainer);

        currentGoalsContainer =
                findViewById(R.id.currentGoalsContainer);

        tvProgressChildName.setText("Loading child profile...");
        tvOverallProgress.setText("Loading progress updates...");
        tvTherapistObservation.setText("Loading observations...");

        // Do not display a fabricated overall percentage.
        progressOverallBar.setVisibility(View.GONE);
    }

    private void loadChildProgress() {

        ApiClient.getApiService().getMyChildren()
                .enqueue(new Callback<List<ParentChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentChildResponse>> call,
                            Response<List<ParentChildResponse>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showError("Unable to load your child's profile.");
                            return;
                        }

                        List<ParentChildResponse> children = response.body();

                        if (children.isEmpty()) {
                            tvProgressChildName.setText("No child linked");
                            tvOverallProgress.setText(
                                    "No progress available yet.");
                            tvTherapistObservation.setText(
                                    "No observations available.");
                            showError("No child is linked to your account.");
                            return;
                        }

                        // Keep the existing flow: use the first linked child.
                        ParentChildResponse child = children.get(0);

                        Long childId = child.getChildId();

                        String childName = child.getChildName();

                        tvProgressChildName.setText(
                                isBlank(childName)
                                        ? "Child Progress"
                                        : childName
                        );

                        if (childId == null) {
                            showError("Child ID is unavailable.");
                            return;
                        }

                        fetchProgress(childId);
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentChildResponse>> call,
                            Throwable t) {

                        Log.e(TAG, "Failed to load children", t);
                        showError(
                                "Network error while loading child profile.");
                    }
                });
    }

    private void fetchProgress(Long childId) {

        progressRepository.getMyChildProgress(
                childId,
                new Callback<List<ProgressRecordResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ProgressRecordResponse>> call,
                            Response<List<ProgressRecordResponse>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Log.e(TAG,
                                    "Progress API failed: " + response.code());

                            tvOverallProgress.setText(
                                    "Progress could not be loaded.");
                            tvTherapistObservation.setText(
                                    "Observations are unavailable.");

                            showError("Unable to load progress records.");
                            return;
                        }

                        List<ProgressRecordResponse> records =
                                response.body();

                        Log.d(TAG,
                                "Progress records loaded: " + records.size());

                        displayProgress(records);
                    }

                    @Override
                    public void onFailure(
                            Call<List<ProgressRecordResponse>> call,
                            Throwable t) {

                        Log.e(TAG, "Failed to load progress records", t);

                        tvOverallProgress.setText(
                                "Progress could not be loaded.");
                        tvTherapistObservation.setText(
                                "Observations are unavailable.");

                        showError(
                                "Network error while loading progress.");
                    }
                });
    }

    private void displayProgress(List<ProgressRecordResponse> records) {

        therapyProgressContainer.removeAllViews();
        recentProgressContainer.removeAllViews();
        currentGoalsContainer.removeAllViews();

        if (records == null || records.isEmpty()) {
            tvOverallProgress.setText(
                    "No progress records available yet.");

            tvTherapistObservation.setText(
                    "No therapist observations available yet.");

            addEmptyMessage(therapyProgressContainer,
                    "Therapy progress will appear here when records are added.");

            addEmptyMessage(recentProgressContainer,
                    "No recent progress updates available.");

            addEmptyMessage(currentGoalsContainer,
                    "No goals have been recorded yet.");

            return;
        }

        // The backend returns the latest records first.
        ProgressRecordResponse latestRecord = records.get(0);

        String latestProgress = latestRecord.getProgress();

        tvOverallProgress.setText(
                isBlank(latestProgress)
                        ? "Latest session update is available below."
                        : "Latest update: " + latestProgress.trim()
        );

        // Display the latest real therapist observation.
        String observation = firstNonBlank(
                latestRecord.getObservations(),
                latestRecord.getRemarks()
        );

        tvTherapistObservation.setText(
                isBlank(observation)
                        ? "No observation recorded for the latest session."
                        : observation.trim()
        );

        // Show each real progress record as a therapy update.
        for (ProgressRecordResponse record : records) {
            addTherapyProgressCard(record);
            addRecentProgressCard(record);
        }

        // Show goals taken from the actual database records.
        addRecordedGoals(records);
    }

    private void addTherapyProgressCard(
            ProgressRecordResponse record) {

        String sessionType = record.getSessionType();

        if (isBlank(sessionType)) {
            sessionType = "Therapy Session";
        }

        String date = formatDate(record.getSessionDate());
        String therapist = record.getTherapistName();
        String progress = record.getProgress();

        MaterialCardView card = createCard();

        LinearLayout content = createVerticalLayout();
        card.addView(content);

        addText(content, sessionType.trim(), 18, true,
                Color.parseColor("#30205F"));

        if (!isBlank(date)) {
            addText(content, "Session date: " + date,
                    13, false, Color.DKGRAY);
        }

        if (!isBlank(therapist)) {
            addText(content, "Therapist: " + therapist.trim(),
                    14, false, Color.DKGRAY);
        }

        if (!isBlank(progress)) {
            addText(content, "Progress: " + progress.trim(),
                    15, true, Color.parseColor("#3F51B5"));

            // Show a bar only when the backend provides a valid percentage.
            Integer percentage = parsePercentage(progress);

            if (percentage != null) {
                ProgressBar bar = new ProgressBar(
                        this,
                        null,
                        android.R.attr.progressBarStyleHorizontal
                );

                LinearLayout.LayoutParams barParams =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                dp(8)
                        );

                barParams.topMargin = dp(8);

                bar.setLayoutParams(barParams);
                bar.setMax(100);
                bar.setProgress(percentage);

                content.addView(bar);
            }
        }

        therapyProgressContainer.addView(card);
    }

    private void addRecentProgressCard(
            ProgressRecordResponse record) {

        MaterialCardView card = createCard();

        LinearLayout content = createVerticalLayout();
        card.addView(content);

        String sessionType = record.getSessionType();

        addText(content,
                isBlank(sessionType)
                        ? "Progress Update"
                        : sessionType.trim(),
                17, true, Color.parseColor("#30205F"));

        String date = formatDate(record.getSessionDate());

        if (!isBlank(date)) {
            addText(content, date, 13, false, Color.GRAY);
        }

        addRecordField(content, "Observations",
                record.getObservations());

        addRecordField(content, "Activities",
                record.getActivities());

        addRecordField(content, "Progress",
                record.getProgress());

        addRecordField(content, "Remarks",
                record.getRemarks());

        String therapist = record.getTherapistName();

        if (!isBlank(therapist)) {
            addText(content, "Therapist: " + therapist.trim(),
                    13, false, Color.DKGRAY);
        }

        recentProgressContainer.addView(card);
    }

    private void addRecordedGoals(
            List<ProgressRecordResponse> records) {

        List<String> uniqueGoals = new ArrayList<>();

        for (ProgressRecordResponse record : records) {

            String goals = record.getGoals();

            if (isBlank(goals)) {
                continue;
            }

            // Split comma-separated goals only; display actual stored text.
            String[] goalItems = goals.split(",");

            for (String item : goalItems) {
                String goal = item.trim();

                if (!goal.isEmpty() && !containsIgnoreCase(
                        uniqueGoals, goal)) {
                    uniqueGoals.add(goal);
                }
            }
        }

        if (uniqueGoals.isEmpty()) {
            addEmptyMessage(currentGoalsContainer,
                    "No goals have been recorded yet.");
            return;
        }

        for (String goal : uniqueGoals) {
            MaterialCardView card = createCard();

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            card.addView(row);

            TextView bullet = new TextView(this);
            bullet.setText("\u2022");
            bullet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            bullet.setTextColor(Color.parseColor("#6554A4"));

            LinearLayout.LayoutParams bulletParams =
                    new LinearLayout.LayoutParams(
                            dp(24),
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            row.addView(bullet, bulletParams);

            TextView goalText = createTextView(
                    goal, 15, false, Color.DKGRAY);

            row.addView(goalText,
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1
                    ));

            currentGoalsContainer.addView(card);
        }
    }

    private void addRecordField(
            LinearLayout container,
            String label,
            String value) {

        if (isBlank(value)) {
            return;
        }

        TextView textView = createTextView(
                label + ": " + value.trim(),
                14,
                false,
                Color.DKGRAY
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(6);

        container.addView(textView, params);
    }

    private MaterialCardView createCard() {

        MaterialCardView card = new MaterialCardView(this);

        card.setRadius(dp(12));
        card.setCardElevation(dp(2));
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(Color.parseColor("#E5E1F0"));
        card.setCardBackgroundColor(Color.WHITE);
        card.setUseCompatPadding(true);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(10);

        card.setLayoutParams(params);

        return card;
    }

    private LinearLayout createVerticalLayout() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(14), dp(12), dp(14), dp(12));

        return layout;
    }

    private void addText(
            LinearLayout container,
            String value,
            int sizeSp,
            boolean bold,
            int color) {

        TextView textView = createTextView(
                value, sizeSp, bold, color);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(5);

        container.addView(textView, params);
    }

    private TextView createTextView(
            String value,
            int sizeSp,
            boolean bold,
            int color) {

        TextView textView = new TextView(this);

        textView.setText(value);
        textView.setTextSize(
                TypedValue.COMPLEX_UNIT_SP, sizeSp);
        textView.setTextColor(color);

        if (bold) {
            textView.setTypeface(
                    null, android.graphics.Typeface.BOLD);
        }

        return textView;
    }

    private void addEmptyMessage(
            LinearLayout container,
            String message) {

        TextView textView = createTextView(
                message, 14, false, Color.GRAY);

        textView.setPadding(dp(8), dp(10), dp(8), dp(10));

        container.addView(textView);
    }

    private Integer parsePercentage(String value) {

        if (isBlank(value)) {
            return null;
        }

        String trimmed = value.trim();

        if (!trimmed.matches("\\d{1,3}\\s*%")) {
            return null;
        }

        try {
            int percentage = Integer.parseInt(
                    trimmed.replace("%", "").trim());

            if (percentage >= 0 && percentage <= 100) {
                return percentage;
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid percentage: " + value);
        }

        return null;
    }

    private String formatDate(String dateTime) {

        if (isBlank(dateTime)) {
            return "";
        }

        String value = dateTime.trim();

        // API example: 2026-10-17T00:00:00
        int separatorIndex = value.indexOf('T');

        if (separatorIndex > 0) {
            return value.substring(0, separatorIndex);
        }

        return value;
    }

    private String firstNonBlank(String first, String second) {

        if (!isBlank(first)) {
            return first;
        }

        if (!isBlank(second)) {
            return second;
        }

        return "";
    }

    private boolean containsIgnoreCase(
            List<String> values,
            String target) {

        for (String value : values) {
            if (value.equalsIgnoreCase(target)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }

    private void showError(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
