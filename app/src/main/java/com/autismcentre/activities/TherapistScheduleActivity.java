
package com.autismcentre.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.TherapistSessionResponse;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistScheduleActivity extends BaseTherapistActivity {

    private LinearLayout sessionContainer;
    private ProgressBar progressSessions;
    private TextView tvScheduleMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageTitle("Rehablito Autism center");
        showPageContent(R.layout.activity_therapist_schedule);

        sessionContainer = findViewById(R.id.sessionContainer);
        progressSessions = findViewById(R.id.progressSessions);
        tvScheduleMessage = findViewById(R.id.tvScheduleMessage);

        loadTherapistSessions();
    }

    private void loadTherapistSessions() {
        progressSessions.setVisibility(View.VISIBLE);
        tvScheduleMessage.setVisibility(View.GONE);
        sessionContainer.removeAllViews();

        ApiClient.getApiService().getMyTherapistSessions()
                .enqueue(new Callback<List<TherapistSessionResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TherapistSessionResponse>> call,
                            Response<List<TherapistSessionResponse>> response) {

                        progressSessions.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            List<TherapistSessionResponse> sessions = response.body();

                            if (sessions.isEmpty()) {
                                showMessage("No sessions assigned to you yet.");
                                return;
                            }

                            tvScheduleMessage.setVisibility(View.GONE);

// ============================================
// SORT SESSIONS BY DATE - LATEST FIRST (DESC)
// ============================================

                            List<TherapistSessionResponse> sortedSessions =
                                    new ArrayList<>(sessions);

                            sortedSessions.sort((session1, session2) -> {

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

                                date1 = date1.trim().substring(
                                        0, Math.min(10, date1.trim().length())
                                );

                                date2 = date2.trim().substring(
                                        0, Math.min(10, date2.trim().length())
                                );

                                return date2.compareTo(date1);
                            });

// Display latest sessions first.
                            for (TherapistSessionResponse session : sortedSessions) {

                                if (session != null) {
                                    addSessionCard(session);
                                }
                            }

                        } else {
                            showMessage("Unable to load sessions. Please try again.");
                            Toast.makeText(
                                    TherapistScheduleActivity.this,
                                    "Request failed: " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistSessionResponse>> call,
                            Throwable t) {

                        progressSessions.setVisibility(View.GONE);
                        showMessage("Could not connect to the server. Check your connection.");
                        Toast.makeText(
                                TherapistScheduleActivity.this,
                                t.getMessage() != null
                                        ? t.getMessage()
                                        : "Network error",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void showMessage(String message) {
        tvScheduleMessage.setText(message);
        tvScheduleMessage.setVisibility(View.VISIBLE);
    }

    private void addSessionCard(TherapistSessionResponse session) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dp(14);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(18));
        card.setCardElevation(dp(2));
        card.setStrokeColor(Color.rgb(233, 221, 240));
        card.setStrokeWidth(dp(1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(18), dp(18), dp(18));

        String startTime = safe(session.getStartTime());
        String endTime = safe(session.getEndTime());

        TextView timeView = createText(
                startTime + " - " + endTime,
                17,
                Color.rgb(23, 23, 23),
                true
        );
        content.addView(timeView);

        TextView statusView = createText(
                "Status: " + safe(session.getStatus()),
                14,
                getStatusColor(session.getStatus()),
                true
        );
        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        statusParams.topMargin = dp(6);
        statusView.setLayoutParams(statusParams);
        content.addView(statusView);

        TextView therapyView = createText(
                safe(session.getTherapyType()),
                17,
                Color.rgb(51, 51, 51),
                true
        );
        LinearLayout.LayoutParams therapyParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        therapyParams.topMargin = dp(10);
        therapyView.setLayoutParams(therapyParams);
        content.addView(therapyView);

        TextView childView = createText(
                "Child: " + safe(session.getChildName()),
                15,
                Color.rgb(85, 85, 85),
                false
        );
        LinearLayout.LayoutParams childParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        childParams.topMargin = dp(6);
        childView.setLayoutParams(childParams);
        content.addView(childView);

        TextView dateView = createText(
                "Date: " + safe(session.getSessionDate()),
                15,
                Color.rgb(85, 85, 85),
                false
        );
        LinearLayout.LayoutParams dateParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        dateParams.topMargin = dp(5);
        dateView.setLayoutParams(dateParams);
        content.addView(dateView);

        MaterialButton viewButton = new MaterialButton(this);
        viewButton.setText("View Session");
        viewButton.setAllCaps(false);
        viewButton.setCornerRadius(dp(12));
        viewButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getColor(R.color.parent_navy)
                )
        );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(50)
                );
        buttonParams.topMargin = dp(16);
        viewButton.setLayoutParams(buttonParams);

        viewButton.setOnClickListener(v -> {
            if (session.getSessionId() == null) {
                Toast.makeText(
                        this,
                        "Session ID is missing.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(
                    TherapistScheduleActivity.this,
                    TherapistSessionDetailsActivity.class
            );

            intent.putExtra("session_id", session.getSessionId());
            startActivity(intent);
        });

        content.addView(viewButton);
        card.addView(content);
        sessionContainer.addView(card);
    }

    private TextView createText(
            String text,
            int sizeSp,
            int color,
            boolean bold) {

        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(null, Typeface.BOLD);
        }

        return view;
    }

    private int getStatusColor(String status) {
        if (status == null) {
            return Color.rgb(117, 117, 117);
        }

        switch (status.toUpperCase()) {
            case "IN_PROGRESS":
                return Color.rgb(230, 126, 34);

            case "COMPLETED":
                return Color.rgb(46, 125, 50);

            case "CANCELLED":
                return Color.rgb(198, 40, 40);

            case "SCHEDULED":
                return Color.rgb(40, 116, 178);

            default:
                return Color.rgb(117, 117, 117);
        }
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "N/A"
                : value;
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.therapist_nav_schedule;
    }
}
