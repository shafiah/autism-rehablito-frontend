
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentSessionResponse;
import com.autismcentre.repository.ParentChildRepository;
import com.autismcentre.repository.ParentSessionRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import android.content.res.ColorStateList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleActivity extends BaseParentActivity {

    private Long childId;
    private LinearLayout sessionListContainer;
    private ParentSessionRepository sessionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_schedule);

        ApiClient.initialize(getApplicationContext());

        sessionListContainer = findViewById(R.id.sessionListContainer);
        sessionRepository = new ParentSessionRepository();

        long receivedChildId = getIntent().getLongExtra("childId", -1L);

        if (receivedChildId > 0) {
            childId = receivedChildId;
            loadSessions();
        } else {
            loadLinkedChild();
        }
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.nav_schedule;
    }

    private void loadLinkedChild() {
        ParentChildRepository childRepository = new ParentChildRepository();

        childRepository.getMyChildren(new Callback<List<com.autismcentre.model.ParentChildResponse>>() {
            @Override
            public void onResponse(
                    Call<List<com.autismcentre.model.ParentChildResponse>> call,
                    Response<List<com.autismcentre.model.ParentChildResponse>> response) {

                if (!response.isSuccessful()
                        || response.body() == null
                        || response.body().isEmpty()) {
                    showMessage("No linked child found.");
                    return;
                }

                childId = response.body().get(0).getChildId();

                if (childId == null || childId <= 0) {
                    showMessage("Child information is unavailable.");
                    return;
                }

                loadSessions();
            }

            @Override
            public void onFailure(
                    Call<List<com.autismcentre.model.ParentChildResponse>> call,
                    Throwable t) {
                showMessage("Unable to load child information.");
            }
        });
    }

    private void loadSessions() {
        if (childId == null || childId <= 0) {
            showMessage("Child information is unavailable.");
            return;
        }

        sessionRepository.getMyChildSessions(
                childId,
                new Callback<List<ParentSessionResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<ParentSessionResponse>> call,
                            Response<List<ParentSessionResponse>> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage("Unable to load therapy schedule.");
                            return;
                        }

                        displaySessions(response.body());
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentSessionResponse>> call,
                            Throwable t) {
                        showMessage("Network error while loading schedule.");
                    }
                });
    }

    private void displaySessions(List<ParentSessionResponse> sessions) {
        sessionListContainer.removeAllViews();

        if (sessions.isEmpty()) {
            TextView emptyMessage = new TextView(this);
            emptyMessage.setText("No therapy sessions scheduled yet.");
            emptyMessage.setTextSize(16);
            emptyMessage.setGravity(Gravity.CENTER);
            emptyMessage.setPadding(24, 32, 24, 32);
            sessionListContainer.addView(emptyMessage);
            return;
        }

        // Latest session date first (DESC order).
        List<ParentSessionResponse> sortedSessions =
                new ArrayList<>(sessions);

        Collections.sort(sortedSessions, (session1, session2) -> {

            if (session1 == null) {
                return 1;
            }

            if (session2 == null) {
                return -1;
            }

            String date1 = session1.getSessionDate();
            String date2 = session2.getSessionDate();

            if (date1 == null || date1.trim().isEmpty()) {
                return 1;
            }

            if (date2 == null || date2.trim().isEmpty()) {
                return -1;
            }

            // Supports yyyy-MM-dd and ISO date-time values.
            date1 = date1.trim().substring(0, Math.min(10, date1.trim().length()));
            date2 = date2.trim().substring(0, Math.min(10, date2.trim().length()));

            return date2.compareTo(date1);
        });

        for (ParentSessionResponse session : sortedSessions) {
            if (session == null) {
                continue;
            }

            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, dpToPx(16));

            card.setLayoutParams(cardParams);
            card.setRadius(dpToPx(12));
            card.setCardElevation(dpToPx(2));
            card.setUseCompatPadding(true);
            card.setCardBackgroundColor(getColor(R.color.parent_card_background));

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(
                    dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

            TextView therapyName = createTextView(
                    valueOrDefault(session.getTherapyType(), "Therapy Session"),
                    18, true);
            content.addView(therapyName);

            content.addView(createTextView(
                    "Date: " + valueOrDefault(session.getSessionDate(), "Not specified"),
                    14, false));

            String startTime = formatTime(session.getStartTime());
            String endTime = formatTime(session.getEndTime());

            content.addView(createTextView(
                    "Time: " + startTime + " - " + endTime,
                    14, false));

            content.addView(createTextView(
                    "Therapist: " + valueOrDefault(session.getTherapistName(), "Not assigned"),
                    14, false));

            content.addView(createTextView(
                    "Branch: " + valueOrDefault(session.getBranchName(), "Not specified"),
                    14, false));

            content.addView(createTextView(
                    "Status: " + valueOrDefault(session.getStatus(), "Not specified"),
                    14, false));

            MaterialButton detailsButton = new MaterialButton(this);
            detailsButton.setText("View Session Details");
            detailsButton.setBackgroundTintList(
                    ColorStateList.valueOf(getColor(R.color.parent_navy))
            );

            LinearLayout.LayoutParams buttonParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonParams.topMargin = dpToPx(8);
            detailsButton.setLayoutParams(buttonParams);

            Long sessionId = session.getSessionId();

            detailsButton.setOnClickListener(v -> {
                if (childId == null || sessionId == null) {
                    showMessage("Session details are unavailable.");
                    return;
                }

                Intent intent = new Intent(
                        ScheduleActivity.this,
                        SessionDetailsActivity.class);
                intent.putExtra("childId", childId);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
            });

            content.addView(detailsButton);
            card.addView(content);
            sessionListContainer.addView(card);
        }
    }

    private TextView createTextView(String text, int size, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(size);
        textView.setPadding(0, dpToPx(4), 0, dpToPx(4));

        if (bold) {
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        return textView;
    }

    private String formatTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return "Not specified";
        }

        // Handles backend time values such as 10:00:00.
        if (time.length() >= 5) {
            return time.substring(0, 5);
        }

        return time;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
