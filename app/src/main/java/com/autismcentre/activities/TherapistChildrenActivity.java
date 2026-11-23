
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
import com.autismcentre.model.TherapistChildResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistChildrenActivity extends BaseTherapistActivity {

    private LinearLayout assignedChildrenContainer;
    private ProgressBar progressAssignedChildren;
    private TextView tvAssignedChildrenMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageTitle("Rehablito Autism Center");
        showPageContent(R.layout.activity_therapist_children);

        assignedChildrenContainer =
                findViewById(R.id.assignedChildrenContainer);

        progressAssignedChildren =
                findViewById(R.id.progressAssignedChildren);

        tvAssignedChildrenMessage =
                findViewById(R.id.tvAssignedChildrenMessage);

        loadAssignedChildren();
    }

    private void loadAssignedChildren() {
        progressAssignedChildren.setVisibility(View.VISIBLE);
        tvAssignedChildrenMessage.setVisibility(View.GONE);
        assignedChildrenContainer.removeAllViews();

        ApiClient.getApiService()
                .getMyTherapistChildren()
                .enqueue(new Callback<List<TherapistChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TherapistChildResponse>> call,
                            Response<List<TherapistChildResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        progressAssignedChildren.setVisibility(View.GONE);

                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage("Unable to load assigned children. Error: "
                                    + response.code());

                            return;
                        }

                        List<TherapistChildResponse> children = response.body();
                        int count = 0;

                        for (TherapistChildResponse child : children) {
                            if (child != null
                                    && child.getChildId() != null
                                    && Boolean.TRUE.equals(child.getActive())) {

                                addChildCard(child);
                                count++;
                            }
                        }

                        if (count == 0) {
                            showMessage("No active children assigned to you.");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistChildResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        progressAssignedChildren.setVisibility(View.GONE);

                        showMessage("Could not connect to the server.");

                        Toast.makeText(
                                TherapistChildrenActivity.this,
                                t.getMessage() != null
                                        ? t.getMessage()
                                        : "Network error",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void addChildCard(TherapistChildResponse child) {
        MaterialCardView card = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dp(14);
        card.setLayoutParams(cardParams);
        card.setRadius(dp(18));
        card.setCardElevation(dp(2));
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeColor(Color.rgb(233, 221, 240));
        card.setStrokeWidth(dp(1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(18), dp(18), dp(18));

        TextView nameView = createText(
                safe(child.getChildName()),
                19,
                Color.rgb(23, 23, 23),
                true
        );
        content.addView(nameView);

        TextView ageView = createText(
                "Age: " + calculateAge(child.getDateOfBirth()),
                15,
                Color.rgb(85, 85, 85),
                false
        );
        addTopMargin(ageView, 6);
        content.addView(ageView);

        TextView branchView = createText(
                "Branch: " + safe(child.getBranchName()),
                15,
                Color.rgb(85, 85, 85),
                false
        );
        addTopMargin(branchView, 4);
        content.addView(branchView);

        MaterialButton viewButton = new MaterialButton(this);
        viewButton.setText(R.string.view_child_details);
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

        buttonParams.topMargin = dp(14);
        viewButton.setLayoutParams(buttonParams);

        viewButton.setOnClickListener(v -> openChildDetails(child));

        content.addView(viewButton);
        card.addView(content);
        assignedChildrenContainer.addView(card);
    }

    private void openChildDetails(TherapistChildResponse child) {
        Intent intent = new Intent(
                TherapistChildrenActivity.this,
                TherapistChildDetailsActivity.class
        );

        intent.putExtra("child_id", child.getChildId());
        intent.putExtra("child_name", child.getChildName());
        intent.putExtra("child_dob", child.getDateOfBirth());
        intent.putExtra("child_gender", child.getGender());
        intent.putExtra("branch_name", child.getBranchName());
        intent.putExtra("medical_notes", child.getMedicalNotes());

        startActivity(intent);
    }

    private void showMessage(String message) {
        tvAssignedChildrenMessage.setText(message);
        tvAssignedChildrenMessage.setVisibility(View.VISIBLE);
    }

    private TextView createText(
            String text,
            int size,
            int color,
            boolean bold) {

        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(null, Typeface.BOLD);
        }

        return view;
    }

    private void addTopMargin(View view, int marginDp) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(marginDp);
        view.setLayoutParams(params);
    }

    private String calculateAge(String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            return "Not available";
        }

        try {
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat(
                            "yyyy-MM-dd",
                            java.util.Locale.US
                    );

            sdf.setLenient(false);

            java.util.Date dob = sdf.parse(dateOfBirth);

            if (dob == null || dob.after(new java.util.Date())) {
                return "Not available";
            }

            java.util.Calendar birth =
                    java.util.Calendar.getInstance();

            birth.setTime(dob);

            java.util.Calendar today =
                    java.util.Calendar.getInstance();

            int years = today.get(
                    java.util.Calendar.YEAR
            ) - birth.get(
                    java.util.Calendar.YEAR
            );

            int currentDay = today.get(
                    java.util.Calendar.DAY_OF_YEAR
            );

            int birthDay = birth.get(
                    java.util.Calendar.DAY_OF_YEAR
            );

            if (currentDay < birthDay) {
                years--;
            }

            return years + (years == 1 ? " Year" : " Years");

        } catch (java.text.ParseException e) {
            return "Not available";
        }
    }
    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "Not available"
                : value;
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.therapist_nav_children;
    }
}
