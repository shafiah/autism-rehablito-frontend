
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.model.ParentChildTherapistResponse;
import com.autismcentre.repository.ParentChildRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistListActivity extends BaseParentActivity {

    private LinearLayout therapistContainer;
    private TextView tvStatus;

    private ParentChildRepository repository;

    private Long childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_therapist_list);

        therapistContainer = findViewById(R.id.therapistContainer);
        tvStatus = findViewById(R.id.tvStatus);

        repository = new ParentChildRepository();

        childId = getIntent().hasExtra("childId")
                ? getIntent().getLongExtra("childId", -1L)
                : -1L;

        if (childId != null && childId > 0) {
            loadAssignedTherapists();
        } else {
            loadLinkedChildren();
        }
    }

    // =========================================
    // LOAD LINKED CHILDREN WHEN CHILD ID MISSING
    // =========================================

    private void loadLinkedChildren() {

        showStatus("Loading child details...");

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

                            if (childId != null && childId > 0) {
                                loadAssignedTherapists();
                            } else {
                                showStatus("Child details not available.");
                            }

                        } else if (response.isSuccessful()) {

                            showStatus(
                                    "No child is linked to your account."
                            );

                        } else {

                            showStatus(
                                    "Unable to load child details. HTTP "
                                            + response.code()
                            );
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

                        showStatus(
                                "Server error while loading child details."
                        );
                    }
                }
        );
    }

    // =========================================
    // LOAD ASSIGNED THERAPISTS
    // =========================================

    private void loadAssignedTherapists() {

        showStatus("Loading assigned therapists...");

        therapistContainer.removeAllViews();

        repository.getMyChildTherapists(
                childId,
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

                            List<ParentChildTherapistResponse> activeTherapists =
                                    new ArrayList<>();

                            for (ParentChildTherapistResponse therapist
                                    : response.body()) {

                                if (therapist != null
                                        && Boolean.TRUE.equals(
                                        therapist.getActive()
                                )
                                        && therapist.getTherapistId() != null) {

                                    activeTherapists.add(therapist);
                                }
                            }

                            displayTherapists(activeTherapists);

                        } else {

                            showStatus(
                                    "Unable to load therapists. HTTP "
                                            + response.code()
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

                        showStatus(
                                "Server error while loading therapists."
                        );
                    }
                }
        );
    }

    // =========================================
    // DISPLAY DYNAMIC THERAPIST CARDS
    // =========================================

    private void displayTherapists(
            List<ParentChildTherapistResponse> therapists
    ) {

        therapistContainer.removeAllViews();

        if (therapists.isEmpty()) {

            showStatus(
                    "No active therapists are currently assigned to your child."
            );

            return;
        }

        tvStatus.setVisibility(View.GONE);

        for (ParentChildTherapistResponse therapist : therapists) {

            addTherapistCard(therapist);
        }
    }

    // =========================================
    // CREATE INDIVIDUAL THERAPIST CARD
    // =========================================

    private void addTherapistCard(
            ParentChildTherapistResponse therapist
    ) {

        MaterialCardView card = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.topMargin = dpToPx(16);

        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(
                getColor(R.color.parent_card_background)
        );
        card.setRadius(dpToPx(20));
        card.setCardElevation(dpToPx(2));

        LinearLayout cardContent = new LinearLayout(this);

        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(
                dpToPx(20),
                dpToPx(20),
                dpToPx(20),
                dpToPx(20)
        );

        String therapistName = therapist.getTherapistName();

        if (therapistName == null || therapistName.trim().isEmpty()) {
            therapistName = "Therapist";
        }

        String specialization = therapist.getTherapyType();

        if (specialization == null || specialization.trim().isEmpty()) {
            specialization = "Therapy";
        }

        TextView tvName = new TextView(this);

        tvName.setText(therapistName);
        tvName.setTextColor(
                getResources().getColor(android.R.color.black)
        );
        tvName.setTextSize(20);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);

        cardContent.addView(tvName);

        TextView tvSpecialization = new TextView(this);

        tvSpecialization.setText(specialization);
        tvSpecialization.setTextColor(
                getResources().getColor(android.R.color.darker_gray)
        );
        tvSpecialization.setTextSize(15);

        LinearLayout.LayoutParams specializationParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        specializationParams.topMargin = dpToPx(6);

        tvSpecialization.setLayoutParams(specializationParams);

        cardContent.addView(tvSpecialization);

        TextView tvAssigned = new TextView(this);

        tvAssigned.setText("Assigned to your child");
        tvAssigned.setTextColor(
                getResources().getColor(R.color.primary)
        );
        tvAssigned.setTextSize(14);
        tvAssigned.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams assignedParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        assignedParams.topMargin = dpToPx(6);

        tvAssigned.setLayoutParams(assignedParams);

        cardContent.addView(tvAssigned);

        MaterialButton btnChat = new MaterialButton(this);

        btnChat.setText("Chat with Therapist");
        btnChat.setCornerRadius(dpToPx(14));
        btnChat.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getColor(R.color.parent_navy)
                )
        );

        btnChat.setTextColor(getColor(R.color.parent_text_white));

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(52)
                );

        buttonParams.topMargin = dpToPx(16);

        btnChat.setLayoutParams(buttonParams);

        Long therapistId = therapist.getTherapistId();

        String finalTherapistName = therapistName;
        String finalSpecialization = specialization;

        btnChat.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TherapistListActivity.this,
                    TherapistChatActivity.class
            );

            intent.putExtra("childId", childId);
            intent.putExtra("therapistId", therapistId);
            intent.putExtra("therapistName", finalTherapistName);
            intent.putExtra("specialization", finalSpecialization);

            startActivity(intent);
        });

        cardContent.addView(btnChat);

        card.addView(cardContent);

        therapistContainer.addView(card);
    }

    // =========================================
    // STATUS MESSAGE
    // =========================================

    private void showStatus(String message) {

        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(message);
    }

    // =========================================
    // DP CONVERSION
    // =========================================

    private int dpToPx(int dp) {

        return (int) (
                dp * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    @Override
    protected int getCurrentBottomNavItem() {

        return R.id.nav_chat;
    }
}
