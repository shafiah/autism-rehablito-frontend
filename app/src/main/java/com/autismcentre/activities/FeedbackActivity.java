
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.model.ParentChildTherapistResponse;
import com.autismcentre.repository.ParentChildRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import android.content.res.ColorStateList;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends BaseParentActivity {

    private static final String TAG = "FeedbackActivity";

    private MaterialAutoCompleteTextView spinnerFeedbackChild;
    private LinearLayout layoutFeedbackTherapists;
    private TextView tvFeedbackTherapistHeading;
    private TextView tvFeedbackEmpty;

    private MaterialButton btnFeedbackHistory;

    private ParentChildRepository repository;

    private final List<ParentChildResponse> children =
            new ArrayList<>();

    private ParentChildResponse selectedChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_feedback);

        ApiClient.initialize(getApplicationContext());

        repository = new ParentChildRepository();

        spinnerFeedbackChild =
                findViewById(R.id.spinnerFeedbackChild);

        layoutFeedbackTherapists =
                findViewById(R.id.layoutFeedbackTherapists);

        tvFeedbackTherapistHeading =
                findViewById(R.id.tvFeedbackTherapistHeading);

        tvFeedbackEmpty =
                findViewById(R.id.tvFeedbackEmpty);

        btnFeedbackHistory =
                findViewById(R.id.btnFeedbackHistory);

        btnFeedbackHistory.setOnClickListener(v -> openFeedbackHistory());

        spinnerFeedbackChild.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position >= 0 && position < children.size()) {

                        selectedChild = children.get(position);

                        loadChildTherapists(selectedChild.getChildId());
                    }
                }
        );

        showLoading("Loading children...");

        loadChildren();
    }

    // =====================================================
    // LOAD PARENT CHILDREN
    // =====================================================

    private void loadChildren() {

        repository.getMyChildren(
                new Callback<List<ParentChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentChildResponse>> call,
                            Response<List<ParentChildResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            children.clear();

                            for (ParentChildResponse child : response.body()) {

                                if (child != null
                                        && child.getChildId() != null
                                        && Boolean.TRUE.equals(child.getActive())) {

                                    children.add(child);
                                }
                            }

                            if (children.isEmpty()) {

                                showEmpty("No active child linked to your account.");

                                return;
                            }

                            setupChildDropdown();

                        } else {

                            Log.e(TAG,
                                    "Children API failed. HTTP: "
                                            + response.code());

                            showEmpty("Unable to load children. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentChildResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(TAG, "Children API request failed", t);

                        showEmpty("Network error while loading children.");
                    }
                }
        );
    }

    // =====================================================
    // SETUP CHILD DROPDOWN
    // =====================================================

    private void setupChildDropdown() {

        List<String> childNames = new ArrayList<>();

        for (ParentChildResponse child : children) {

            String name = child.getChildName();

            if (name == null || name.trim().isEmpty()) {
                name = "Child " + child.getChildId();
            }

            childNames.add(name);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        childNames
                );

        spinnerFeedbackChild.setAdapter(adapter);

        spinnerFeedbackChild.setText(
                childNames.get(0),
                false
        );

        selectedChild = children.get(0);

        loadChildTherapists(selectedChild.getChildId());
    }

    // =====================================================
    // LOAD SELECTED CHILD THERAPISTS
    // =====================================================

    private void loadChildTherapists(Long childId) {

        if (childId == null || childId <= 0) {
            showEmpty("Child details not available.");
            return;
        }

        layoutFeedbackTherapists.removeAllViews();

        tvFeedbackTherapistHeading.setText("Assigned Therapists");

        showLoading("Loading assigned therapists...");

        repository.getMyChildTherapists(
                childId,
                new Callback<List<ParentChildTherapistResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentChildTherapistResponse>> call,
                            Response<List<ParentChildTherapistResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        // Ignore an older response if user selected another child.
                        if (selectedChild == null
                                || !childId.equals(selectedChild.getChildId())) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            layoutFeedbackTherapists.removeAllViews();

                            List<ParentChildTherapistResponse> activeTherapists =
                                    new ArrayList<>();

                            for (ParentChildTherapistResponse therapist
                                    : response.body()) {

                                if (therapist != null
                                        && therapist.getTherapistId() != null
                                        && Boolean.TRUE.equals(therapist.getActive())) {

                                    activeTherapists.add(therapist);
                                }
                            }

                            if (activeTherapists.isEmpty()) {

                                showEmpty("No active therapists assigned to this child.");

                                return;
                            }

                            tvFeedbackEmpty.setVisibility(View.GONE);

                            for (ParentChildTherapistResponse therapist
                                    : activeTherapists) {

                                addTherapistCard(therapist);
                            }

                            Log.d(TAG,
                                    "Active therapists loaded: "
                                            + activeTherapists.size());

                        } else {

                            Log.e(TAG,
                                    "Therapist API failed. HTTP: "
                                            + response.code());

                            showEmpty("Unable to load assigned therapists.");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentChildTherapistResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (selectedChild == null
                                || !childId.equals(selectedChild.getChildId())) {
                            return;
                        }

                        Log.e(TAG, "Therapist API request failed", t);

                        showEmpty("Network error while loading therapists.");
                    }
                }
        );
    }

    // =====================================================
    // CREATE DYNAMIC THERAPIST CARD
    // =====================================================

    private void addTherapistCard(
            ParentChildTherapistResponse therapist) {

        MaterialCardView card = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, dp(8), 0, dp(8));

        card.setLayoutParams(cardParams);
        card.setRadius(dp(14));
        card.setCardElevation(dp(2));

        card.setStrokeWidth(dp(1));

        card.setStrokeColor(
                ContextCompat.getColor(this, R.color.parent_navy)
        );

        card.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.parent_card_background)
        );

        LinearLayout content = new LinearLayout(this);

        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView tvName = new TextView(this);

        final String therapistName =
                (therapist.getTherapistName() == null
                        || therapist.getTherapistName().trim().isEmpty())
                        ? "Therapist"
                        : therapist.getTherapistName().trim();

        tvName.setText(therapistName);
        tvName.setTextSize(17);
        tvName.setTextColor(
                ContextCompat.getColor(this, R.color.parent_navy)
        );
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvTherapy = new TextView(this);

        final String therapyType =
                (therapist.getTherapyType() == null
                        || therapist.getTherapyType().trim().isEmpty())
                        ? "Therapy"
                        : therapist.getTherapyType().trim();

        tvTherapy.setText(therapyType);
        tvTherapy.setTextSize(14);
        tvTherapy.setTextColor(
                ContextCompat.getColor(this, R.color.parent_text_secondary)
        );

        LinearLayout.LayoutParams therapyParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        therapyParams.topMargin = dp(5);

        tvTherapy.setLayoutParams(therapyParams);

        MaterialButton btnGiveFeedback = new MaterialButton(this);

        btnGiveFeedback.setText("Give Feedback");
        btnGiveFeedback.setAllCaps(false);

        btnGiveFeedback.setBackgroundTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(
                                this,
                                R.color.parent_navy
                        )
                )
        );

        btnGiveFeedback.setTextColor(
                ContextCompat.getColor(
                        this,
                        android.R.color.white
                )
        );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        buttonParams.topMargin = dp(12);

        btnGiveFeedback.setLayoutParams(buttonParams);

        btnGiveFeedback.setOnClickListener(v ->
                openFeedbackForm(
                        therapist.getTherapistId(),
                        therapistName,
                        therapyType
                )
        );

        content.addView(tvName);
        content.addView(tvTherapy);
        content.addView(btnGiveFeedback);

        card.addView(content);

        layoutFeedbackTherapists.addView(card);
    }

    // =====================================================
    // OPEN FEEDBACK FORM
    // =====================================================

    private void openFeedbackForm(
            Long therapistId,
            String therapistName,
            String specialization) {

        if (selectedChild == null
                || selectedChild.getChildId() == null
                || therapistId == null) {

            Toast.makeText(
                    this,
                    "Child or therapist details unavailable.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent = new Intent(
                FeedbackActivity.this,
                FeedbackFormActivity.class
        );

        intent.putExtra("childId", selectedChild.getChildId());
        intent.putExtra(
                "childName",
                selectedChild.getChildName()
        );
        intent.putExtra("therapistId", therapistId);

        intent.putExtra("therapistName", therapistName);
        intent.putExtra("specialization", specialization);

        startActivity(intent);
    }

    // =====================================================
    // OPEN FEEDBACK HISTORY
    // =====================================================

    private void openFeedbackHistory() {

        Intent intent = new Intent(
                FeedbackActivity.this,
                FeedbackHistoryActivity.class
        );

        if (selectedChild != null
                && selectedChild.getChildId() != null) {

            intent.putExtra("childId", selectedChild.getChildId());
        }

        startActivity(intent);
    }

    // =====================================================
    // LOADING STATE
    // =====================================================

    private void showLoading(String message) {

        layoutFeedbackTherapists.removeAllViews();

        tvFeedbackEmpty.setText(message);
        tvFeedbackEmpty.setVisibility(View.VISIBLE);
    }

    // =====================================================
    // EMPTY / ERROR STATE
    // =====================================================

    private void showEmpty(String message) {

        layoutFeedbackTherapists.removeAllViews();

        tvFeedbackEmpty.setText(message);
        tvFeedbackEmpty.setVisibility(View.VISIBLE);
    }

    // =====================================================
    // DP CONVERSION
    // =====================================================

    private int dp(int value) {

        return (int) (
                value * getResources()
                        .getDisplayMetrics().density
        );
    }

    // =====================================================
    // BOTTOM NAVIGATION
    // =====================================================

    @Override
    protected int getCurrentBottomNavItem() {

        return R.id.nav_feedback;
    }
}
